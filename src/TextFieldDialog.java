import javax.swing.*;
import java.awt.*;

public class TextFieldDialog extends JDialog {
    private final JTextField textField;
    private String savedText = "";

    TextFieldDialog(JFrame owner, String s) {
        super(owner, "Enter Text", true);
        setLayout(new FlowLayout());
        setSize(300, 120);

        textField = new JTextField(s,20);
        add(textField);

        JButton saveButton = new JButton("Load");
        saveButton.addActionListener(e -> {
            savedText = textField.getText();
            setVisible(false);
        });
        add(saveButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> setVisible(false));
        add(cancelButton);
    }

    String getSavedText() {
        return savedText;
    }
}
