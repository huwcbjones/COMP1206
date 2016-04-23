package shared.components;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Allows output streams to be output to a text area
 *
 * @author Huw Jones
 * @since 09/11/2015
 */
public class JTextAreaOutputStream extends OutputStream {
    private JTextArea _textArea;

    public JTextAreaOutputStream(JTextArea textArea){
        this._textArea = textArea;
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
     *
     * @param b the <code>byte</code>.
     * @throws IOException if an I/O error occurs. In particular,
     *                     an <code>IOException</code> may be thrown if the
     *                     output stream has been closed.
     */
    @Override
    public void write(int b) throws IOException {
        _textArea.append(String.valueOf((char)b));
        _textArea.setCaretPosition(_textArea.getDocument().getLength());
    }
}
