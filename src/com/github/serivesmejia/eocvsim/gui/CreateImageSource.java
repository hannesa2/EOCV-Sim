package com.github.serivesmejia.eocvsim.gui;

import com.github.serivesmejia.eocvsim.gui.util.GuiUtil;
import com.github.serivesmejia.eocvsim.input.ImageSource;
import com.github.serivesmejia.eocvsim.input.InputSourceManager;
import com.github.serivesmejia.eocvsim.util.CvUtil;
import org.opencv.core.Size;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class CreateImageSource {

    public JDialog createImageSource = null;

    public JTextField imgDirTextField = null;
    public JTextField widthTextField = null;
    public JTextField heightTextField = null;

    public JButton createButton = null;

    InputSourceManager sourceManager = null;
    public boolean selectedValidImage = false;

    public CreateImageSource(JFrame parent, InputSourceManager sourceManager) {

        createImageSource = new JDialog(parent);
        this.sourceManager = sourceManager;

        initCreateImageSource();

    }

    public void initCreateImageSource() {

        createImageSource.setModal(true);

        createImageSource.setTitle("Create image source");
        createImageSource.setSize(350, 200);

        JPanel contentsPanel = new JPanel(new GridLayout(4, 1));

        JPanel imgDirPanel = new JPanel(new FlowLayout());

        imgDirTextField = new JTextField(18);
        imgDirTextField.setEditable(false);
        JButton selectDirButton = new JButton("Select file...");

        imgDirPanel.add(imgDirTextField);
        imgDirPanel.add(selectDirButton);

        contentsPanel.add(imgDirPanel);

        // Size part

        JPanel sizePanel = new JPanel(new FlowLayout());

        JLabel sizeLabel = new JLabel("Size: ");
        sizeLabel.setHorizontalAlignment(JLabel.LEFT);

        widthTextField = new JTextField("580",4);

        sizePanel.add(sizeLabel);
        sizePanel.add(widthTextField);

        JLabel xSizeLabel = new JLabel(" x ");
        xSizeLabel.setHorizontalAlignment(JLabel.CENTER);

        heightTextField = new JTextField("480", 4);

        sizePanel.add(xSizeLabel);
        sizePanel.add(heightTextField);

        contentsPanel.add(sizePanel);

        contentsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        sizePanel.add(xSizeLabel);
        sizePanel.add(heightTextField);

        contentsPanel.add(sizePanel);

        contentsPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));

        //Name part

        JPanel namePanel = new JPanel(new FlowLayout());

        JLabel nameLabel = new JLabel("Source name: ");
        sizeLabel.setHorizontalAlignment(JLabel.LEFT);

        JTextField nameTextField = new JTextField("ImageSource-" + (sourceManager.sources.size() + 1),15);

        namePanel.add(nameLabel);
        namePanel.add(nameTextField);

        contentsPanel.add(namePanel);

        // Bottom buttons

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        createButton = new JButton("Create");
        createButton.setEnabled(selectedValidImage);

        buttonsPanel.add(createButton);

        JButton cancelButton = new JButton("Cancel");
        buttonsPanel.add(cancelButton);

        contentsPanel.add(buttonsPanel);

        //Add contents

        createImageSource.getContentPane().add(contentsPanel, BorderLayout.CENTER);

        // Additional stuff & events

        selectDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Images", "jpg", "jpeg", "jpe", "jp2","bmp", "png", "tiff", "tif");
                chooser.setFileFilter(filter);

                int returnVal = chooser.showOpenDialog(createImageSource);

                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    imageFileSelected(chooser.getSelectedFile());
                }

            }
        });

        GuiUtil.jTextFieldOnlyNumbers(widthTextField, 0, 580);
        GuiUtil.jTextFieldOnlyNumbers(heightTextField, 0, 480);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int width = Integer.parseInt(widthTextField.getText());
                int height = Integer.parseInt(heightTextField.getText());
                createSource(nameTextField.getText(), imgDirTextField.getText(), new Size(width, height));
                close();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });

        createImageSource.setResizable(false);
        createImageSource.setLocationRelativeTo(null);
        createImageSource.setVisible(true);

    }

    public void imageFileSelected(File f) {

        String fileAbsPath = f.getAbsolutePath();

        if(CvUtil.checkImageValid(fileAbsPath)) {
            imgDirTextField.setText(fileAbsPath);
            selectedValidImage = true;
        } else {
            imgDirTextField.setText("Unable to load selected file.");
            selectedValidImage = false;
        }

        createButton.setEnabled(selectedValidImage);

    }

    public void close() {
        createImageSource.setVisible(false);
        createImageSource.dispose();
    }

    public void createSource(String sourceName, String imgPath, Size size) {
        sourceManager.requestToAddInputSource(sourceName, new ImageSource(imgPath, size));
        while(!sourceManager.finishedAddingRequestedSource) {}
        sourceManager.requestInputSourceListUpdate();
    }

}
