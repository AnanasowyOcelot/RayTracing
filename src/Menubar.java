import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class Menubar extends JMenuBar {

    public static JLabel labelka;

    public Menubar(GamePanel panel){

        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        final GamePanel p = panel;
        JMenu file = new JMenu("Menu");
        add(file);
        JMenuItem exit = new JMenuItem("Exit");
        file.add(exit);
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        JMenuItem reset = new JMenuItem("Reset");
        file.add(reset);
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                p.reset();
            }
        });

        this.setLayout(new GridLayout());

        labelka = new JLabel("Liczba punktów: " + panel.getPointsCollected() + " ");
//        labelka.setAlignmentX(Box.RIGHT_ALIGNMENT);
        labelka.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        add(labelka);

        JMenuItem pause = new JMenuItem("Pause");
        file.add(pause);
        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                p.pause();
            }
        });

    }

}
