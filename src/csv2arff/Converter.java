package csv2arff;

import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import javax.swing.*;

public class Converter implements ActionListener {

	JButton browseButton, goButton;
	JFileChooser fc;
	JFrame frame;
	String selectedFile = "";
	String writeLoc = "";
	String fileName = "";
	JTextField attsTxt, typesTxt;
	JCheckBox header;

	public static void main(String[] args) {
		Converter c = new Converter();
	}

	public Converter() {
		fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		browseButton = new JButton("Browse");
		goButton = new JButton("Convert");
		attsTxt = new JTextField(50);
		typesTxt = new JTextField(50);
		header = new JCheckBox();
		JLabel attsLab = new JLabel("Attributes (, separated): ");
		JLabel typesLab = new JLabel("Types (, separated): ");
		JLabel headerLab = new JLabel("Header? ");
		browseButton.addActionListener(this);
		goButton.addActionListener(this);

		// Create panels
		JPanel fieldPanelAttr = new JPanel(new FlowLayout());
		fieldPanelAttr.add(attsLab);
		fieldPanelAttr.add(attsTxt);

		JPanel fieldPanelTypes = new JPanel(new FlowLayout());
		fieldPanelTypes.add(typesLab);
		fieldPanelTypes.add(typesTxt);

		JPanel fieldPanelHeader = new JPanel(new FlowLayout());
		fieldPanelHeader.add(headerLab);
		fieldPanelHeader.add(header);

		JPanel fieldsPanel = new JPanel(new BorderLayout());
		fieldsPanel.add(fieldPanelAttr, BorderLayout.NORTH);
		fieldsPanel.add(fieldPanelTypes, BorderLayout.CENTER);
		fieldsPanel.add(fieldPanelHeader, BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.add(browseButton, BorderLayout.NORTH);
		buttonPanel.add(goButton, BorderLayout.CENTER);

		JPanel finalPanel = new JPanel(new BorderLayout());
		finalPanel.add(fieldsPanel, BorderLayout.CENTER);
		finalPanel.add(buttonPanel, BorderLayout.SOUTH);

		// Set up the window
		frame = new JFrame("CSV to ARFF");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(finalPanel);

		frame.pack();
		frame.setVisible(true);
	}

	// Read CSV file line-by-line

	public Boolean convert(String readFile, String writeFile, String title, String attributes, String types,
			Boolean header) {
		BufferedReader bufrdr = null;
		PrintWriter fileWriter = null;
		String thisLine = "";
		String[] lineParts = null;
		int variableCount = 0;

		// Open writer
		try {
			fileWriter = new PrintWriter(writeFile + ".arff", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			bufrdr = new BufferedReader(new FileReader(readFile));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			return false;
		}
		

		// Convert attributes string to array
		String[] attributeArr = attributes.split(",");
		String[] typesArr = types.split(",");

		// Check that sizes are the same and match data
		if (attributeArr.length != typesArr.length) {
			if (bufrdr != null) {
				try {
					bufrdr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return false;
		}

		// Set variable count
		// variableCount = attributeArr.length;

		// Write ARFF header
		fileWriter.println("@RELATION " + title);
		fileWriter.println();
		// Loop through attributes
		for (int i = 0; i < attributeArr.length; i++) {
			fileWriter.println("@ATTRIBUTE " + attributeArr[i] + " " + typesArr[i].toUpperCase());
		}

		fileWriter.println();

		// Write data declaration
		fileWriter.println("@DATA");

		// Loop through each line in CSV, convert
		int lineID = 0;
		try {
			while ((thisLine = bufrdr.readLine()) != null) {
				// Continue if header
				if (lineID == 0 && header == true)
					continue;

				lineParts = thisLine.split(",");
				for (int i = 0; i < lineParts.length; i++) {

					// Set to ? if null
					if (lineParts[i] == "")
						lineParts[i] = "?";

					// If this is is last element end line, comma if not
					if (i == lineParts.length - 1) {
						fileWriter.print(lineParts[i] + "\r");
					} else {
						fileWriter.print(lineParts[i] + ",");
					}
				}
				lineID++;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Close reader and printer

		if (bufrdr != null) {
			try {
				bufrdr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (fileWriter != null) {
			fileWriter.close();
		}

		return true;
	}

	// Event listener for GUI
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == browseButton) {
			int returnVal = fc.showOpenDialog(this.frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				this.fileName = file.getName();
				this.selectedFile = file.getPath();
				this.writeLoc = file.getParent() + "/" + this.fileName;
			}
		} else {
			// Go button
			if (this.selectedFile.length() > 0) {
				Boolean success = convert(this.selectedFile, this.writeLoc, this.fileName, this.attsTxt.getText(), this.typesTxt.getText(), this.header.isSelected());
				if (success) JOptionPane.showMessageDialog(null, "Coverted successfully!");
			}
		}
	}
}