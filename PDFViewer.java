// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   PDFViewer.java

package com.pdfviewer;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

// Referenced classes of package com.pdfviewer:
//            EditPageScreen

public class PDFViewer extends JFrame
{

    public PDFViewer()
    {
        selectedPage = 0;
        selectMultiple = false;
        startPageEdit = -1;
        endPageEdit = -1;
        getContentPane().setLayout(null);
        getContentPane().setBackground(Color.GRAY);
        pdfPane = new JScrollPane();
        pdfPane.setVerticalScrollBarPolicy(22);
        pdfPane.setSize(610, 228);
        pdfPane.setBackground(Color.GRAY);
        getContentPane().add(pdfPane);
        pdfPane.revalidate();
        pdfPane.repaint();
        pdfViewPanel = new JPanel();
        pdfViewPanel.setLocation(0, 0);
        pdfViewPanel.setBackground(Color.DARK_GRAY);
        pdfPane.add(pdfViewPanel);
        panel = new JPanel();
        panel.setBounds(10, 11, 610, 23);
        panel.setBackground(Color.GRAY);
        getContentPane().add(panel);
        panel.setLayout(new GridLayout(0, 5, 3, 5));
        textFileField = new JTextPane();
        textFileField.setEnabled(false);
        textFileField.setText("pdf file");
        panel.add(textFileField);
        btnDisplay = new JButton("Update Display");
        panel.add(btnDisplay);
        btnSavePdf = new JButton("Save PDF");
        panel.add(btnSavePdf);
        pageSelectBox = new JComboBox();
        pageSelectBox.addItemListener(itemListener);
        panel.add(pageSelectBox);
        updateText = new JTextPane();
        updateText.setEditable(false);
        updateText.setBackground(Color.GRAY);
        panel.add(updateText);
        editOptions = new JPanel();
        editOptions.setBounds(10, 239, 610, 30);
        editOptions.setBackground(Color.GRAY);
        getContentPane().add(editOptions);
        GridBagLayout gbl_editOptions = new GridBagLayout();
        gbl_editOptions.columnWidths = (new int[] {
            100, 100, 100, 0, 0
        });
        gbl_editOptions.rowHeights = (new int[] {
            30, 0
        });
        gbl_editOptions.columnWeights = (new double[] {
            0.0D, 0.0D, 0.0D, 0.0D, 4.9406564584124654E-324D
        });
        gbl_editOptions.rowWeights = (new double[] {
            0.0D, 4.9406564584124654E-324D
        });
        editOptions.setLayout(gbl_editOptions);
        multiSheetSelect = new JCheckBox("Multiple Sheet Select");
        multiSheetSelect.setForeground(Color.WHITE);
        multiSheetSelect.setBackground(Color.GRAY);
        GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
        gbc_chckbxNewCheckBox.fill = 1;
        gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 0, 5);
        gbc_chckbxNewCheckBox.gridx = 0;
        gbc_chckbxNewCheckBox.gridy = 0;
        editOptions.add(multiSheetSelect, gbc_chckbxNewCheckBox);
        startPage = new JTextField();
        startPage.setForeground(Color.WHITE);
        startPage.setBackground(Color.GRAY);
        startPage.setEditable(false);
        GridBagConstraints gbc_startPage = new GridBagConstraints();
        gbc_startPage.fill = 1;
        gbc_startPage.insets = new Insets(0, 0, 0, 5);
        gbc_startPage.gridx = 1;
        gbc_startPage.gridy = 0;
        editOptions.add(startPage, gbc_startPage);
        startPage.setColumns(1);
        endPage = new JTextField();
        endPage.setBackground(Color.GRAY);
        endPage.setForeground(Color.WHITE);
        endPage.setEditable(false);
        GridBagConstraints gbc_endPage = new GridBagConstraints();
        gbc_endPage.insets = new Insets(0, 0, 0, 5);
        gbc_endPage.fill = 1;
        gbc_endPage.gridx = 2;
        gbc_endPage.gridy = 0;
        editOptions.add(endPage, gbc_endPage);
        endPage.setColumns(1);
        JButton editPageButton = new JButton("Open Editor");
        editPageButton.setBackground(Color.DARK_GRAY);
        editPageButton.setForeground(Color.WHITE);
        editPageButton.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e)
            {
                setupPageEditor();
            }
        });
        GridBagConstraints gbc_editPageButton = new GridBagConstraints();
        gbc_editPageButton.gridx = 3;
        gbc_editPageButton.gridy = 0;
        editOptions.add(editPageButton, gbc_editPageButton);
        pageScreen = new EditPageScreen();
        pageScreen.setSize(400, 400);
        pageScreen.setVisible(true);
        initAction();
    }

    private void initAction()
    {
        btnDisplay.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e)
            {
                mouseWorker.execute();
            }
        });
        multiSheetSelect.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e)
            {
                selectMultiple = true;
            }
        });
        btnSavePdf.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent mouseevent)
            {
            }
        });
        textFileField.addMouseListener(new MouseAdapter() {

            public void mouseClicked(MouseEvent e)
            {
                textFileField.setEnabled(true);
                textFileField.setText("");
                textFileField.requestFocus();
            }
        });
        addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e)
            {
                dispose();
                System.exit(0);
            }
        });
        pdfViewPanel.setDropTarget(new DropTarget() {

            public synchronized void drop(DropTargetDropEvent evt)
            {
                try
                {
                    evt.acceptDrop(1);
                    java.util.List transferData = extracted(evt);
                    java.util.List droppedFiles = transferData;
                    File fileHolder = null;
                    for(Iterator iterator = droppedFiles.iterator(); iterator.hasNext();)
                    {
                        File file = (File)iterator.next();
                        if(file.getAbsolutePath().contains("C:"))
                        {
                            pdfFile = file.getAbsolutePath();
                            textFileField.setText(file.getAbsolutePath());
                            fileHolder = file;
                        }
                    }

                    if(fileHolder != null)
                        mouseWorker.execute();
                }
                catch(Exception exception) { }
            }

            private java.util.List extracted(DropTargetDropEvent evt)
                throws UnsupportedFlavorException, IOException
            {
                java.util.List transferData = (java.util.List)evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                return transferData;
            }
        }
);
        itemListener = new ItemListener() {

            public void itemStateChanged(ItemEvent evt)
            {
                if(pages != null && evt.getStateChange() == 1)
                {
                    selectedPage = ((Integer)evt.getItem()).intValue();
                    try
                    {
                        updateText.setText((new StringBuilder("Loading Page ")).append(selectedPage + 1).toString());
                        mouseWorker.execute();
                    }
                    catch(Exception exception) { }
                }
            }
        };
        addComponentListener(new ComponentAdapter() {

            public void componentResized(ComponentEvent e)
            {
                pdfPane.setBounds(0, 45, getWidth() - 15, getHeight() - (int)panel.getAlignmentY() - panel.getHeight() - editOptions.getHeight() - 45 - 31);
                pdfViewPanel.setBounds(0, 0, pdfPane.getWidth(), pdfPane.getHeight() - 80);
                pdfPane.setViewportView(pdfViewPanel);
                pdfViewPanel.setSize(pdfPane.getHeight() - 80, pdfPane.getWidth() * 300);
                editOptions.setBounds(0, pdfPane.getHeight() + panel.getHeight() + (int)panel.getAlignmentY() + (int)pdfPane.getAlignmentY() + 30, getWidth() - 15, 31);
            }
        });
        mouseClickThread = new Thread() {

            public void run()
            {
                processMouseClick();
                repaint();
            }
        };
        mouseWorker = new SwingWorker() {

            protected Object doInBackground()
                throws Exception
            {
                mouseClickThread.run();
                return null;
            }
        };
        pageEditorWorker = new SwingWorker() {

            protected Object doInBackground()
            {
                try
                {
                    System.out.println("Opening EditPageScreen");
                    int end = 0;
                    int start;
                    if(startPageEdit > endPageEdit)
                    {
                        start = endPageEdit;
                        end = startPageEdit;
                    } else
                    {
                        start = startPageEdit;
                        end = endPageEdit;
                    }
                    int pages[] = new int[Math.abs(startPageEdit - endPageEdit)];
                    for(int i = start; i < end; i++)
                        pages[i] = i;

                    pageScreen.runWorker(pages, pdfFile);
                }
                catch(Exception ex)
                {
                    System.out.println(ex);
                }
                return null;
            }
        };
    }

    private void setupPageEditor()
    {
        try
        {
            pageEditorWorker.execute();
        }
        catch(Exception ex)
        {
            System.out.println(ex);
        }
    }

    private void processMouseClick()
    {
        try
        {
            String pdfFileString = textFileField.getText();
            File file = null;
            if(pdfFileString.contains("http://") || pdfFileString.contains("https://"))
            {
                updateText.setText("Downloading PDF");
                processHTML(pdfFileString, file);
            } else
            {
                pdfFile = textFileField.getText();
                file = new File(pdfFile);
                processPDF(file);
            }
        }
        catch(Exception exception) { }
    }

    private void processHTML(String pdfFileString, File file)
        throws Exception
    {
        URL url = new URL(pdfFileString);
        URLConnection v = url.openConnection();
        InputStream input = v.getInputStream();
        pdfFile = "tempFile.pdf";
        file = new File(pdfFile);
        if(!file.exists())
        {
            file.createNewFile();
        } else
        {
            file.delete();
            file.createNewFile();
        }
        OutputStream output = new FileOutputStream(file);
        byte data[] = new byte[1024];
        for(int i = 0; (i = input.read(data)) > 0;)
            output.write(data);

        output.close();
        input.close();
        processPDF(file);
    }

    private void processPDF(File file) throws Exception{
        updateText.setText("Loading PDF File");
        System.out.println("Something else");
        document = PDDocument.load(file);
        System.out.println("something");
        pages = document.getDocumentCatalog().getPages();
        System.out.println("something else");
        if(pages != null) {
            PDPage testPage = pages.get(selectedPage);
            pageSelectBox.removeItemListener(itemListener);
            updateText.setText("Loading Page Items");
            pageSelectBox.removeAllItems();
            pdfViewPanel.removeAll();
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            System.out.println(pages.getCount());
            for(int i = 0; i < pages.getCount(); i++) {
                pageSelectBox.addItem(Integer.valueOf(i));
                BufferedImage bim = pdfRenderer.renderImage(i);
                JLabel label = new JLabel();
                if(bim.getWidth() > 0) {
                    int height = 0;
                    height = pdfPane.getHeight() - 60;
                    int width = Math.round(((float)bim.getWidth() / (float)bim.getHeight()) * (float)height);
                    java.awt.Image tmp = bim.getScaledInstance(width, height, 4);
                    BufferedImage dimg = new BufferedImage(width, height, 2);
                    Graphics2D g2d = dimg.createGraphics();
                    g2d.drawImage(tmp, 0, 0, null);
                    g2d.dispose();
                    label.setBounds(0, 0, width, height);
                    label.setIcon(new ImageIcon(dimg));
                    label.setLayout(new BoxLayout(label, 1));
                    label.setBackground(Color.WHITE);
                    final int page = i;
                    label.addMouseListener(new MouseListener() {
                        public void mouseClicked(MouseEvent evt) {
                            if(selectMultiple) {
                                if(startPageEdit == -1) startPageEdit = page;
                                endPageEdit = startPageEdit;
                                startPageEdit = page;
                                startPage.setText(String.valueOf(startPageEdit));
                                endPage.setText(String.valueOf(endPageEdit));
                                processSelected();
                            }
                        }

                        public void mousePressed(MouseEvent mouseevent)
                        {
                        }

                        public void mouseReleased(MouseEvent mouseevent)
                        {
                        }

                        public void mouseEntered(MouseEvent e)
                        {
                            e.getComponent().setBackground(new Color(0, 0, 0, 100));
                        }

                        public void mouseExited(MouseEvent e)
                        {
                            e.getComponent().setBackground(new Color(0, 0, 0, 255));
                        }
                    });
                    pdfViewPanel.add(label);
                }
                bim.flush();
            }

            pageSelectBox.addItemListener(itemListener);
            PDFTextStripper pdftextstripper = new PDFTextStripper() {

                protected void writeString(String text, java.util.List textPositions) throws IOException{
                    TextPosition firstPosition = (TextPosition)textPositions.get(0);
                    writeString(String.format("[%s]", new Object[] {
                        Float.valueOf(firstPosition.getXDirAdj())
                    }));
                    super.writeString(text, textPositions);
                }
            };
        }
        document.close();
        updateText.setText("Completed");
    }

    public void updatePage(int pageNumber, PDPage page, int x, int y) throws Exception{
        PDPageContentStream contentStream = new PDPageContentStream(document, page, org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode.APPEND, true);
        contentStream.beginText();
        contentStream.moveTo(x, y);
        contentStream.showText("Testing");
        contentStream.endText();
        contentStream.close();
    }

    public void processSelected()
    {
        Component components[] = pdfViewPanel.getComponents();
        int l = startPageEdit <= endPageEdit ? startPageEdit : endPageEdit;
        int r = startPageEdit >= endPageEdit ? startPageEdit : endPageEdit;
        Border b = new LineBorder(Color.ORANGE, 4, true);
        for(int i = startPageEdit; i < endPageEdit; i++) {
            ((JPanel)components[i]).setBorder(b);
        }
    }

    public static void main(String arg[])
    {
        PDFViewer pdfViewer = new PDFViewer();
        int x = 90;
        int y = 90;
        int width = 700;
        int height = 300;
        pdfViewer.setBounds(x, y, width, height);
        pdfViewer.setVisible(true);
    }

    private static final long serialVersionUID = 0x96e771edb6d7e349L;
    private JTextPane textFileField;
    private PDPageTree pages;
    private PDDocument document;
    private JPanel pdfViewPanel;
    private JPanel panel;
    private JComboBox pageSelectBox;
    private ItemListener itemListener;
    private int selectedPage;
    private int selectedPages[];
    private String pdfFile;
    private Canvas canvas;
    private JScrollPane pdfPane;
    private Thread mouseClickThread;
    private SwingWorker mouseWorker;
    private SwingWorker pageEditorWorker;
    private JButton btnSavePdf;
    private JButton btnDisplay;
    private JTextPane updateText;
    private JTextField startPage;
    private JTextField endPage;
    private JPanel editOptions;
    private JCheckBox multiSheetSelect;
    private EditPageScreen pageScreen;
    private boolean selectMultiple;
    private int startPageEdit;
    private int endPageEdit;
}