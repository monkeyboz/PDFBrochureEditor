// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   EditPageScreen.java

package com.pdfviewer;

import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class EditPageScreen extends JFrame
{

    public EditPageScreen()
    {
        getContentPane().setBackground(Color.GRAY);
        setBackground(Color.GRAY);
        pageHolder = new JPanel();
        pageHolder.setLayout(new BoxLayout(pageHolder, 0));
        editOptions = new JPanel();
        editOptions.setBackground(Color.DARK_GRAY);
        GroupLayout groupLayout = new GroupLayout(getContentPane());
        groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(pageHolder, -2, 434, -2).addGroup(groupLayout.createSequentialGroup().addGap(6).addComponent(editOptions, -2, 434, -2))).addContainerGap(-1, 32767)));
        groupLayout.setVerticalGroup(groupLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(groupLayout.createSequentialGroup().addComponent(pageHolder, -2, 177, -2).addGap(52).addComponent(editOptions, -2, 60, -2).addGap(28)));
        JPanel saveChanges = new JPanel();
        editOptions.add(saveChanges);
        JPanel addImage = new JPanel();
        editOptions.add(addImage);
        JPanel addText = new JPanel();
        editOptions.add(addText);
        getContentPane().setLayout(groupLayout);
        action();
    }

    public void runWorker(int pages[], String pdfFile)
    {
        pageNumbers = pages;
        pdfString = pdfFile;
        SwingWorker worker = new SwingWorker() {

            protected Object doInBackground()
                throws Exception
            {
                processPDF();
                return null;
            }
        }
;
        worker.run();
    }

    public void displayPage(int i)
    {
        try
        {
            pageHolder.removeAll();
            PDDocument pdf = PDDocument.load(new File(pdfString));
            PDFRenderer renderer = new PDFRenderer(pdf);
            BufferedImage bim = renderer.renderImage(i);
            int height = pageHolder.getHeight();
            int width = Math.round(((float)bim.getWidth() / (float)bim.getHeight()) * (float)height);
            java.awt.Image image = bim.getScaledInstance(width, height, 2);
            JLabel pageImage = new JLabel();
            pageImage.setBounds(0, 0, width, height);
            pageImage.setIcon(new ImageIcon(image));
            pageHolder.add(pageImage);
            PDFTextStripper testStripper = new PDFTextStripper() {

                protected void writeString(String text, java.util.List textPositions)
                    throws IOException
                {
                    TextPosition firstPosition = (TextPosition)textPositions.get(0);
                    writeString(String.format("[%s]", new Object[] {
                        Float.valueOf(firstPosition.getXDirAdj())
                    }));
                    super.writeString(text, textPositions);
                }

            };
            pdf.close();
            repaint();
        }
        catch(Exception exception) { }
    }

    public void displayPageWorker(final int i)
    {
        SwingWorker worker = new SwingWorker() {

            public Object doInBackground()
            {
                displayPage(i);
                return null;
            }
        };
        worker.execute();
    }

    public void processPDF()
    {
        pdf = new File(pdfString);
        if(pdf.exists())
            try
            {
                PDDocument pdfDocument = PDDocument.load(pdf);
                pages = new ArrayList();
                for(int i = 0; i < pageNumbers.length; i++)
                    pages.add(pdfDocument.getPage(pageNumbers[i]));

                pdfDocument.close();
                displayPageWorker(0);
            }
            catch(Exception ex)
            {
                System.out.println(ex);
            }
    }

    public void action()
    {
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                dispose();
                System.exit(0);
            }
        });
        addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e)
            {
                pageHolder.setBounds(0, 0, getWidth(), getHeight() - editOptions.getHeight());
                editOptions.setBounds(0, pageHolder.getHeight() - editOptions.getHeight(), getWidth(), editOptions.getHeight());
            }
        });
    }

    public Rectangle getPageRectangle()
    {
        if(pages.size() > 0)
        {
            Rectangle rect = new Rectangle(0, 0, (int)((PDPage)pages.get(0)).getMediaBox().getWidth(), (int)((PDPage)pages.get(0)).getMediaBox().getHeight());
            return rect;
        } else
        {
            return new Rectangle();
        }
    }

    private ArrayList pages;
    private File pdf;
    private String pdfString;
    private int start;
    private int end;
    private JPanel editOptions;
    private JPanel pageHolder;
    private int pageNumbers[];
}