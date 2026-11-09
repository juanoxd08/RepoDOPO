package presentation;

import domain.*;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Ventana GUI principal del juego POOBSoccer.
 */
public class HeadSoccerGUI extends JFrame implements KeyListener, ActionListener {
    private HeadSoccer game;
    private PhotoHeadSoccer photo;
    private Timer gameLoop;
    private Set<Integer> activeKeys;

    /**
     * Construye la ventana principal e inicializa el bucle de juego.
     */
    public HeadSoccerGUI() {
        super("POOBSoccer 2026-2 - Head Soccer Arcade");
        this.game = new HeadSoccer();
        this.photo = new PhotoHeadSoccer(game);
        this.activeKeys = new HashSet<>();

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BorderLayout());
        this.add(photo, BorderLayout.CENTER);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setResizable(false);

        this.addKeyListener(this);
        this.setFocusable(true);

        this.gameLoop = new Timer(20, this);
        this.gameLoop.start();
    }

    /**
     * Ejecuta una iteracion del bucle de juego y redibuja la vista.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        processContinuousKeys();
        game.tick();
        photo.repaint();
    }

    /*
     * Procesa las teclas de movimiento continuo mantenidas presionadas.
     */
    private void processContinuousKeys() {
        if (game.getState() != GameStatus.PLAYING && game.getState() != GameStatus.GOLDEN_GOAL) return;

        if (activeKeys.contains(KeyEvent.VK_A)) {
            game.getPlayer1().moveLeft();
        }
        if (activeKeys.contains(KeyEvent.VK_D)) {
            game.getPlayer1().moveRight();
        }

        if (game.getController2() instanceof HumanController) {
            if (activeKeys.contains(KeyEvent.VK_LEFT)) {
                game.getPlayer2().moveLeft();
            }
            if (activeKeys.contains(KeyEvent.VK_RIGHT)) {
                game.getPlayer2().moveRight();
            }
        }
    }

    /**
     * Captura las teclas presionadas para ejecutar acciones de juego o control.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        activeKeys.add(code);

        if (code == KeyEvent.VK_P) {
            game.togglePause();
        } else if (code == KeyEvent.VK_R) {
            game.reset();
        }

        if (game.getState() == GameStatus.PLAYING || game.getState() == GameStatus.GOLDEN_GOAL) {
            if (code == KeyEvent.VK_W) {
                game.getPlayer1().jump();
            } else if (code == KeyEvent.VK_S) {
                game.getPlayer1().kick();
            } else if (code == KeyEvent.VK_Q) {
                game.getPlayer1().usePower(game.getBall(), game.getPlayer2());
            }

            if (game.getController2() instanceof HumanController) {
                if (code == KeyEvent.VK_UP) {
                    game.getPlayer2().jump();
                } else if (code == KeyEvent.VK_DOWN) {
                    game.getPlayer2().kick();
                } else if (code == KeyEvent.VK_CONTROL) {
                    game.getPlayer2().usePower(game.getBall(), game.getPlayer1());
                }
            }
        }
    }

    /**
     * Procesa la liberacion de teclas para detener acciones.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        activeKeys.remove(code);

        if (code == KeyEvent.VK_S) {
            if (game.getPlayer1() != null) game.getPlayer1().stopKick();
        }
        if (code == KeyEvent.VK_DOWN) {
            if (game.getPlayer2() != null && game.getController2() instanceof HumanController) {
                game.getPlayer2().stopKick();
            }
        }
    }

    /**
     * Maneja eventos de caracteres tipados.
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

    /**
     * Punto de entrada principal para iniciar la interfaz grafica.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HeadSoccerGUI ventana = new HeadSoccerGUI();
            ventana.setVisible(true);
        });
    }
}
