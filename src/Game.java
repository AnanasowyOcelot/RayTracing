import javax.swing.*;

public class Game {


    public static void main(String[] args){
        final JFrame window = new JFrame("Crocodile Adventures");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        final GamePanel panel = new GamePanel();
        window.setContentPane(panel);
        window.setResizable(false);
        window.setVisible(true);
        window.setAlwaysOnTop(true);

        // MENU
        Menubar menubar = new Menubar(panel);
        window.setJMenuBar(menubar);

        window.pack();

    }
}
