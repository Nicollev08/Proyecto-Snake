import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.sound.sampled.*;

public class SnakeGameApp {
    public static void main(String[] args) {
        
        playSound("SOUNDS/SonidoInicio.wav", false);

        JFrame frame = new JFrame("Juego Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        JButton btnClasic = new JButton("Modo clasico");
        JButton btn2Players = new JButton("Modo dos jugadores");
        JButton btnSalir = new JButton("Salir");

        btnClasic.addActionListener(e -> startGame(frame, 1));
        btn2Players.addActionListener(e -> startGame(frame, 2));
        btnSalir.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "¡Gracias por jugar!");
            System.exit(0);
        });

        panel.add(btnClasic);
        panel.add(btn2Players);
        panel.add(btnSalir);

        frame.add(panel);
        frame.setVisible(true);
    }

    private static void startGame(JFrame frame, int players) {
        playSound("SOUNDS/SonidoJuego.wav", true);
        SnakeGame game = new SnakeGame(700, 600, players, frame);
        frame.getContentPane().removeAll();
        frame.getContentPane().add(game);
        frame.revalidate();
        frame.repaint();
        game.requestFocus();
    }

    public static void playSound(String soundFile, boolean loop) {
        try {
            File soundPath = new File(soundFile);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    clip.start();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class SnakeGame extends JPanel implements ActionListener, KeyListener {
    class Tile {
        int x, y;

        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    class Player {
        Tile head;
        ArrayList<Tile> body;
        int velocityX, velocityY, growAmount;

        Player(int startX, int startY) {
            head = new Tile(startX, startY);
            body = new ArrayList<>();
            velocityX = 1;
            velocityY = 0;
        }
    }

    int players;
    int boardWidth, boardHeight, tileSize = 31;
    Tile food;
    Random random;
    Image fruitImage, headImage, bodyImage;
    Timer gameLoop;
    boolean gameOver = false;
    JFrame frame;
    Player player1, player2;

    // Agregar variables para las imágenes del jugador 2
    Image headImage2, bodyImage2, fruitImage2;

    SnakeGame(int boardWidth, int boardHeight, int players, JFrame frame) {

        this.players = players;
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        this.frame = frame;
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        try {
            // Cargar imágenes del jugador 1
            fruitImage = ImageIO.read(new File("IMG/apple.png"));
            headImage = ImageIO.read(new File("IMG/head.png"));
            bodyImage = ImageIO.read(new File("IMG/body.png"));

            // Cargar imágenespara el modo de juego 2
            fruitImage2 = ImageIO.read(new File("IMG/mango.png"));
            headImage2 = ImageIO.read(new File("IMG/head2.png"));
            bodyImage2 = ImageIO.read(new File("IMG/body2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        player1 = new Player(10, 10);
        if (players == 2) {
            player2 = new Player(5, 5);
        }

        food = new Tile(10, 10);
        random = new Random();
        placeFood();

        gameLoop = new Timer(100, this);
        gameLoop.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {

        // Dibujar el marco amarillo alrededor de la cuadrícula
        g.setColor(Color.yellow);
        int gridX = 2;
        int gridY = 73; // Ajuste para el desplazamiento vertical
        int gridWidth = (boardWidth / tileSize) * tileSize;
        int gridHeight = (boardHeight / tileSize) * tileSize;
        g.drawRect(gridX, gridY, gridWidth, gridHeight);

        // Dibujar la cuadrícula
        g.setColor(Color.gray); // Cambiar color si es necesario
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 75, i * tileSize, boardHeight * 10); // Ajuste vertical
            g.drawLine(0, i * tileSize + 75, boardWidth, i * tileSize + 75); // Ajuste horizontal
        }

        // Dibujar la comida y los jugadores
        Image currentFruitImage = (players == 2) ? fruitImage2 : fruitImage;
        g.drawImage(currentFruitImage, food.x * tileSize, food.y * tileSize + 75, tileSize, tileSize, this);
        drawPlayer(g, player1, headImage, bodyImage);
        if (player2 != null) {
            drawPlayer(g, player2, headImage2, bodyImage2);
        }

        // Mostrar puntajes y "Game Over"
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.setColor(Color.red);
            g.drawString("Game Over!", tileSize * 10, tileSize + 13); // Mostrar Game Over en la pantalla
            SwingUtilities.invokeLater(() -> showGameOverDialog());
        } else {
            // Puntaje del Jugador 1 (serpiente verde) en el lado derecho
            g.setColor(Color.green);
            g.drawString("Player 1 Score: " + player1.body.size(), tileSize * 12, tileSize + 13);

            // Puntaje del Jugador 2 (serpiente azul) en el lado izquierdo
            if (player2 != null) {
                g.setColor(Color.blue);
                g.drawString("Player 2 Score: " + player2.body.size(), tileSize * 6, tileSize + 13);
            }
        }

    }

    // Mostrar "GameOver" cuando el jugador pierda
    public void showGameOverDialog() {
        if (!gameOver)
            return; // Asegúrate de que el juego haya terminado

        StringBuilder message = new StringBuilder("PUNTAJES\n");
        message.append("Jugador 1: ").append(player1.body.size()).append("\n");

        if (players == 2 && player2 != null) {
            message.append("Jugador 2: ").append(player2.body.size()).append("\n");
        }

        message.append("¿Quieres jugar de nuevo?");

        // Mostrar el cuadro de diálogo con los puntajes
        int option = JOptionPane.showConfirmDialog(frame, message.toString(), "Fin del juego",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    private void drawPlayer(Graphics g, Player player, Image headImg, Image bodyImg) {
        g.drawImage(headImg, player.head.x * tileSize, player.head.y * tileSize + 75, tileSize, tileSize, this);
        for (Tile part : player.body) {
            g.drawImage(bodyImg, part.x * tileSize, part.y * tileSize + 75, tileSize, tileSize, this);
        }
    }

    public void placeFood() {
        food.x = random.nextInt(boardWidth / tileSize);
        food.y = random.nextInt(boardHeight / tileSize);
    }

    public static void playSound(String soundFile) {
        try {
            File soundPath = new File(soundFile);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start();
            } else {
                System.out.println("Archivo de sonido no encontrado");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void move() {
        movePlayer(player1);
        if (player2 != null) {
            movePlayer(player2);
        }

        // Verificar colisiones con la comida
        if (collision(player1.head, food)) {
            player1.growAmount++;
            placeFood();
            playSound("SOUNDS/SonidoComer.wav");
        }
        if (player2 != null && collision(player2.head, food)) {
            player2.growAmount++;
            placeFood();
            playSound("SOUNDS/SonidoComer.wav");
        }

        // Comprobar colisiones que terminan el 
        if (collisionWithBody(player1.head, player1.body) || player1.head.x < 0
                || player1.head.x >= boardWidth / tileSize
                || player1.head.y < 0 || player1.head.y >= boardHeight / tileSize) {
            gameOver = true;
        }
        if (player2 != null && (collisionWithBody(player2.head, player2.body) || player2.head.x < 0
                || player2.head.x >= boardWidth / tileSize
                || player2.head.y < 0 || player2.head.y >= boardHeight / tileSize)) {
            gameOver = true;
        }
        if (player2 != null && (collision(player1.head, player2.head) || collisionWithBody(player1.head, player2.body)
                || collisionWithBody(player2.head, player1.body))) {
            gameOver = true;
        }
    }

    public void movePlayer(Player player) {
        int newX = player.head.x + player.velocityX;
        int newY = player.head.y + player.velocityY;

        // Verificar que la cabeza no se salga de los límites de la cuadrícula
        if (newX < 0 || newX >= boardWidth / tileSize || newY < 0 || newY >= boardHeight / tileSize) {
            gameOver = true;
            return;
        }

        // Mover las partes del cuerpo
        for (int i = player.body.size() - 1; i >= 1; i--) {
            player.body.set(i, player.body.get(i - 1));
        }
        if (player.body.size() > 0) {
            player.body.set(0, new Tile(player.head.x, player.head.y));
        }
        player.head = new Tile(newX, newY);

        // Hacer crecer la serpiente cuando come comida
        if (player.growAmount > 0) {
            player.body.add(new Tile(-1, -1)); // Añadir una nueva parte al cuerpo
            player.growAmount--;
        }
    }

    public boolean collision(Tile a, Tile b) {
        return a.x == b.x && a.y == b.y;
    }

    public boolean collisionWithBody(Tile head, ArrayList<Tile> body) {
        for (Tile part : body) {
            if (collision(head, part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            move();
            repaint();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_UP && player1.velocityY == 0) {
            player1.velocityX = 0;
            player1.velocityY = -1;
        }
        if (key == KeyEvent.VK_DOWN && player1.velocityY == 0) {
            player1.velocityX = 0;
            player1.velocityY = 1;
        }
        if (key == KeyEvent.VK_LEFT && player1.velocityX == 0) {
            player1.velocityX = -1;
            player1.velocityY = 0;
        }
        if (key == KeyEvent.VK_RIGHT && player1.velocityX == 0) {
            player1.velocityX = 1;
            player1.velocityY = 0;
        }
        if (player2 != null) {
            if (key == KeyEvent.VK_W && player2.velocityY == 0) {
                player2.velocityX = 0;
                player2.velocityY = -1;
            }
            if (key == KeyEvent.VK_S && player2.velocityY == 0) {
                player2.velocityX = 0;
                player2.velocityY = 1;
            }
            if (key == KeyEvent.VK_A && player2.velocityX == 0) {
                player2.velocityX = -1;
                player2.velocityY = 0;
            }
            if (key == KeyEvent.VK_D && player2.velocityX == 0) {
                player2.velocityX = 1;
                player2.velocityY = 0;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public void restartGame() {
        player1 = new Player(10, 10);
        if (players == 2) {
            player2 = new Player(5, 5);
        }
        food = new Tile(10, 10);
        placeFood();
        gameOver = false;
        repaint();
        gameLoop.start();
    }

}
