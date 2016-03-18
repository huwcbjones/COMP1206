import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

/**
 * File Browser
 *
 * @author Huw Jones
 * @since 18/03/2016
 */
public class FileBrowser extends JFrame {

    private JButton btn_open;
    private JTextArea text_display;

    public FileBrowser() {
        super("File Browser");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {

        }
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setMinimumSize(new Dimension(800, 600));

        this.setLayout(new BorderLayout());

        text_display = new JTextArea("");
        text_display.setEditable(false);

        JScrollPane scroller = new JScrollPane(text_display);

        this.add(scroller, BorderLayout.CENTER);

        btn_open = new JButton("Browse");
        btn_open.addActionListener(new documentBrowser());
        this.add(btn_open, BorderLayout.PAGE_END);

        this.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileBrowser fb = new FileBrowser();
        });
    }

    private class documentBrowser implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            int returnValue = fc.showOpenDialog(FileBrowser.this);

            if (returnValue != JFileChooser.APPROVE_OPTION) return;

            File file = fc.getSelectedFile();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                text_display.setText("");
                String line;
                try {
                    while((line = reader.readLine()) != null){
                        text_display.append(line + "\n");
                    }
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            } catch (FileNotFoundException e1) {
                JOptionPane.showMessageDialog(
                        FileBrowser.this,
                        "Failed to open file.\n" + e1.getMessage(),
                        "Failed to open file.",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

}
