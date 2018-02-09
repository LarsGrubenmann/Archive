package client.gui;

import client.controller.Controller;
import def.Constraints;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

/**
 *
 * @author Lars
 */
public class LoginView extends JPanel {

    private JTextField jtfName;
    private JTextField jtfServer;
    private JButton jbOK;

    public LoginView(final Controller c) {
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder(Constraints.BORDER, "Login"));
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        CaretListener cl = new CaretListener() {

            public void caretUpdate(CaretEvent e) {
                if (jtfServer.getText().equals("") || jtfName.getText().equals("")) {
                    jbOK.setEnabled(false);
                } else {
                    jbOK.setEnabled(true);
                }
            }
        };
        ActionListener al = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!jtfServer.getText().equals("") && !jtfName.getText().equals("")) {
                    String clientName = jtfName.getText();
                    String serverName = jtfServer.getText();
                    c.login(clientName, serverName);
                }
            }
        };
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Name"), gbc);
        gbc.gridx = 1;
        jtfName = new JTextField();
        jtfName.setColumns(24);
        jtfName.addCaretListener(cl);
        jtfName.addActionListener(al);
        add(jtfName, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Server"), gbc);
        gbc.gridx = 1;
        jtfServer = new JTextField();
        jtfServer.setColumns(24);
        jtfServer.addCaretListener(cl);
        jtfServer.addActionListener(al);
        add(jtfServer, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        //jbOK = new JButton("ok");
        jbOK = new JButton();
        jbOK.setIcon(Constraints.getResourceIcon("login_normal.png"));
        jbOK.setDisabledIcon(Constraints.getResourceIcon("login_mute.png"));
        jbOK.setPressedIcon(Constraints.getResourceIcon("login_down.png"));
        jbOK.setRolloverIcon(Constraints.getResourceIcon("login_marked.png"));
        jbOK.setSelectedIcon(Constraints.getResourceIcon("login_down"));
        jbOK.setFocusPainted(false);
        jbOK.setContentAreaFilled(false);
        jbOK.addActionListener(al);
        add(jbOK, gbc);
    }

    public JTextField getJtfName() {
        return jtfName;
    }

    public JTextField getJtfServer() {
        return jtfServer;
    }

    public JButton getJbOK() {
        return jbOK;
    }
}
