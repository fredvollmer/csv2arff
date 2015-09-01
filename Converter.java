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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;

import com.sun.javafx.collections.MappingChange.Map;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class Converter implements ActionListener {

	JButton browseButton, goButton;
	JFileChooser fc;
	JFrame frame;
	String selectedFile = "";
	String writeLoc = "";
	String fileName = "";
	JTextField attsTxt, typesTxt, classTxt;
	JCheckBox header;
	int classElement;

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
		classTxt = new JTextField(5);
		header = new JCheckBox();
		JLabel attsLab = new JLabel("Attributes (, separated): ");
		JLabel typesLab = new JLabel("Types (, separated): ");
		JLabel headerLab = new JLabel("Header? ");
		JLabel classLab = new JLabel("Class field number (0 represents first variable): ");
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
		// fieldPanelHeader.add(classLab);
		// fieldPanelHeader.add(classTxt);
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
		HashMap<Integer, Attribute> attributeList = new HashMap<Integer, Attribute>(); // Keys
																						// are
																						// variable
																						// number
		Set<Integer> nominals = new HashSet<Integer>();
		Attribute attr;
		String dataText = "";

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

			// Create Attribute for each instance, add to nominal set if
			// categorical
			attributeList.put(i, attr = new Attribute(attributeArr[i], typesArr[i]));
			if (typesArr[i].compareTo("nominal") == 0)
				nominals.add(i);
		}

		// Write data declaration
		dataText += "@DATA\n";

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

					// If this is categorical/nominal variable, update attribute
					if (nominals.contains(i)) {
						attributeList.get(i).addCategory(lineParts[i]);
					}

					// If this is is last element end line, comma if not
					if (i == lineParts.length - 1) {
						dataText += (lineParts[i] + "\r");
					} else {
						dataText += (lineParts[i] + ",");
					}
				}
				lineID++;
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Print attributes.
		for (Attribute at : attributeList.values()) {
			fileWriter.print("@ATTRIBUTE " + at.name + " ");
			// If categories isn't null, print categories. Otherwise print type.
			if (at.hasCats()) {
				fileWriter.println(at.categories());
			} else {
				fileWriter.println(at.type());
			}
		}

		fileWriter.println();

		// Print data text
		fileWriter.print(dataText);

		// Close printer

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
				Boolean success = convert(this.selectedFile, this.writeLoc, this.fileName, this.attsTxt.getText(),
						this.typesTxt.getText(), this.header.isSelected());
				if (success) {
					JOptionPane.showMessageDialog(null, "Coverted successfully!");
				} else {
					JOptionPane.showMessageDialog(null, "An error occured :(");
				}
			}
		}
	}
}