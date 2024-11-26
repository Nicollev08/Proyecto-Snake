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
        // Reproduce un sonido inicial al comenzar la aplicación
        playSound("SOUNDS/SonidoInicio.wav", false);

        // Configuración de la ventana principal del juego
        JFrame frame = new JFrame("Juego Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 700);
        frame.setLocationRelativeTo(null); // Centrar la ventana en la pantalla

        // Crear un panel con un diseño de cuadrícula para los botones del menú
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(3, 1));

        // Crear botones para las opciones del menú
        JButton btnClasic = new JButton("Modo clasico");
        JButton btn2Players = new JButton("Modo dos jugadores");
        JButton btnSalir = new JButton("Salir");

        // Asignar acciones a los botones
        btnClasic.addActionListener(e -> startGame(frame, 1)); // Inicia el juego en modo clásico
        btn2Players.addActionListener(e -> startGame(frame, 2)); // Inicia el juego en modo dos jugadores
        btnSalir.addActionListener(e -> { // Sale del juego
            JOptionPane.showMessageDialog(frame, "¡Gracias por jugar!");
            System.exit(0);
        });

        // Agregar los botones al panel
        panel.add(btnClasic);
        panel.add(btn2Players);
        panel.add(btnSalir);

        // Agregar el panel a la ventana principal y mostrarla
        frame.add(panel);
        frame.setVisible(true);
    }

    // Método para iniciar el juego
    private static void startGame(JFrame frame, int players) {
        playSound("SOUNDS/SonidoJuego.wav", true); // Reproduce música de fondo del juego
        SnakeGame game = new SnakeGame(700, 600, players, frame); // Crear el juego con la configuración especificada
        frame.getContentPane().removeAll(); // Limpiar el contenido de la ventana
        frame.getContentPane().add(game); // Agregar el panel del juego
        frame.revalidate(); // Actualizar la ventana
        frame.repaint(); // Repintar la ventana
        game.requestFocus(); // Asegurarse de que el panel del juego reciba eventos de teclado
    }

    // Método para reproducir sonidos
    public static void playSound(String soundFile, boolean loop) {
        try {
            File soundPath = new File(soundFile);
            if (soundPath.exists()) {
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY); // Repetir el sonido si es necesario
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
        Tile head; // Posición de la cabeza de la serpiente
        ArrayList<Tile> body; // Segmentos del cuerpo
        int velocityX, velocityY; // Velocidad de movimiento (dirección)
        int growAmount; // Cantidad de segmentos por crecer

        Player(int startX, int startY) {
            head = new Tile(startX, startY); // Inicializa la cabeza en una posición
            body = new ArrayList<>(); // Inicializa el cuerpo vacío
            velocityX = 1; // Comienza moviéndose hacia la derecha
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
        this.players = players; // Número de jugadores
        this.boardWidth = boardWidth; // Ancho del tablero
        this.boardHeight = boardHeight; // Altura del tablero
        this.frame = frame;

        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.black); // Fondo negro del tablero
        addKeyListener(this); // Agregar soporte para teclas
        setFocusable(true); // Asegurar que pueda recibir eventos de teclado

        try {
            // Cargar imágenes de comida, cabeza y cuerpo de la serpiente
            fruitImage = ImageIO.read(new File("IMG/apple.png"));
            headImage = ImageIO.read(new File("IMG/head.png"));
            bodyImage = ImageIO.read(new File("IMG/body.png"));

            // Cargar imágenes adicionales para el modo de 2 jugadores
            fruitImage2 = ImageIO.read(new File("IMG/mango.png"));
            headImage2 = ImageIO.read(new File("IMG/head2.png"));
            bodyImage2 = ImageIO.read(new File("IMG/body2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        player1 = new Player(10, 10); // Inicializa el primer jugador
        if (players == 2) {
            player2 = new Player(5, 5); // Inicializa el segundo jugador si aplica
        }

        food = new Tile(10, 10); // Posición inicial de la comida
        random = new Random();
        placeFood(); // Coloca la comida en una posición aleatoria

        gameLoop = new Timer(100, this); // Bucle principal del juego
        gameLoop.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g); // Llama al método personalizado para dibujar
    }

    public void draw(Graphics g) {
        // Dibujar marco alrededor del tablero
        g.setColor(Color.yellow);
        g.drawRect(2, 73, (boardWidth / tileSize) * tileSize, (boardHeight / tileSize) * tileSize);

        // Dibujar cuadrícula
        g.setColor(Color.gray);
        for (int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 75, i * tileSize, boardHeight * 10); // Líneas verticales
            g.drawLine(0, i * tileSize + 75, boardWidth, i * tileSize + 75); // Líneas horizontales
        }

        // Dibujar comida y jugadores
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
        // Dibuja la cabeza del jugador en su posición actual
        g.drawImage(headImg, player.head.x * tileSize, player.head.y * tileSize + 75, tileSize, tileSize, this);

        // Itera sobre las partes del cuerpo del jugador y las dibuja
        for (Tile part : player.body) {
            g.drawImage(bodyImg, part.x * tileSize, part.y * tileSize + 75, tileSize, tileSize, this);
        }
    }

    public void placeFood() {
        // Genera una posición aleatoria dentro de los límites del tablero para la
        // comida
        food.x = random.nextInt(boardWidth / tileSize);
        food.y = random.nextInt(boardHeight / tileSize);
    }

    public static void playSound(String soundFile) {
        try {
            File soundPath = new File(soundFile); // Ruta al archivo de sonido
            if (soundPath.exists()) {
                // Leer el archivo de sonido y configurarlo para reproducir
                AudioInputStream audioInput = AudioSystem.getAudioInputStream(soundPath);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInput);
                clip.start(); // Comienza la reproducción
            } else {
                System.out.println("Archivo de sonido no encontrado");
            }
        } catch (Exception ex) {
            ex.printStackTrace(); // Imprime cualquier error que ocurra
        }
    }

    public void move() {
        // Mueve al jugador 1
        movePlayer(player1);

        // Si hay un segundo jugador, también lo mueve
        if (player2 != null) {
            movePlayer(player2);
        }

        // Verifica si el jugador 1 colisiona con la comida
        if (collision(player1.head, food)) {
            player1.growAmount++; // Incrementa el tamaño que debe crecer la serpiente
            placeFood(); // Genera nueva comida
            playSound("SOUNDS/SonidoComer.wav"); // Reproduce el sonido de comer
        }

        // Verifica si el jugador 2 (si existe) colisiona con la comida
        if (player2 != null && collision(player2.head, food)) {
            player2.growAmount++;
            placeFood();
            playSound("SOUNDS/SonidoComer.wav");
        }

        // Verifica si hay colisiones que terminan el juego
        if (collisionWithBody(player1.head, player1.body) // Colisión con su propio cuerpo
                || player1.head.x < 0 || player1.head.x >= boardWidth / tileSize // Fuera del tablero horizontal
                || player1.head.y < 0 || player1.head.y >= boardHeight / tileSize) { // Fuera del tablero vertical
            gameOver = true; // Termina el juego
        }

        // Colisiones para el jugador 2
        if (player2 != null && (collisionWithBody(player2.head, player2.body) // Colisión con su propio cuerpo
                || player2.head.x < 0 || player2.head.x >= boardWidth / tileSize // Fuera del tablero horizontal
                || player2.head.y < 0 || player2.head.y >= boardHeight / tileSize)) { // Fuera del tablero vertical
            gameOver = true;
        }

        // Colisiones entre jugadores (solo en modo 2 jugadores)
        if (player2 != null && (collision(player1.head, player2.head) // Colisión de cabezas
                || collisionWithBody(player1.head, player2.body) // Jugador 1 colisiona con el cuerpo del jugador 2
                || collisionWithBody(player2.head, player1.body))) { // Jugador 2 colisiona con el cuerpo del jugador 1
            gameOver = true;
        }
    }

    public void movePlayer(Player player) {
        // Calcula las nuevas coordenadas de la cabeza del jugador basándose en la
        // dirección actual
        int newX = player.head.x + player.velocityX;
        int newY = player.head.y + player.velocityY;

        // Verifica que la cabeza del jugador no se salga de los límites de la
        // cuadrícula
        if (newX < 0 || newX >= boardWidth / tileSize || newY < 0 || newY >= boardHeight / tileSize) {
            gameOver = true; // Termina el juego si la cabeza se sale de los límites
            return;
        }

        // Mueve las partes del cuerpo del jugador
        for (int i = player.body.size() - 1; i >= 1; i--) {
            player.body.set(i, player.body.get(i - 1)); // Desplaza cada segmento hacia la posición del segmento
                                                        // anterior
        }

        // Si la serpiente tiene partes, actualiza la posición del primer segmento a la
        // antigua posición de la cabeza
        if (player.body.size() > 0) {
            player.body.set(0, new Tile(player.head.x, player.head.y));
        }

        // Actualiza la posición de la cabeza con las nuevas coordenadas calculadas
        player.head = new Tile(newX, newY);

        // Verifica si el jugador debe crecer
        if (player.growAmount > 0) {
            player.body.add(new Tile(-1, -1)); // Añade una nueva parte al cuerpo en una posición temporal
            player.growAmount--; // Reduce la cantidad pendiente de crecimiento
        }
    }

    public boolean collision(Tile a, Tile b) {
        // Comprueba si dos "Tiles" ocupan la misma posición
        return a.x == b.x && a.y == b.y;
    }

    public boolean collisionWithBody(Tile head, ArrayList<Tile> body) {
        // Recorre cada segmento del cuerpo para comprobar si la cabeza colisiona con
        // alguno
        for (Tile part : body) {
            if (collision(head, part)) {
                return true; // Retorna verdadero si hay colisión
            }
        }
        return false; // No hubo colisión
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) { // Solo actúa si el juego no ha terminado
            move(); // Actualiza la lógica del juego (probablemente mueve todos los jugadores y
                    // procesa eventos)
            repaint(); // Redibuja el tablero para reflejar los cambios
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode(); // Obtiene el código de la tecla presionada

        // Controla el movimiento del primer jugador
        if (key == KeyEvent.VK_UP && player1.velocityY == 0) { // Movimiento hacia arriba
            player1.velocityX = 0;
            player1.velocityY = -1;
        }
        if (key == KeyEvent.VK_DOWN && player1.velocityY == 0) { // Movimiento hacia abajo
            player1.velocityX = 0;
            player1.velocityY = 1;
        }
        if (key == KeyEvent.VK_LEFT && player1.velocityX == 0) { // Movimiento hacia la izquierda
            player1.velocityX = -1;
            player1.velocityY = 0;
        }
        if (key == KeyEvent.VK_RIGHT && player1.velocityX == 0) { // Movimiento hacia la derecha
            player1.velocityX = 1;
            player1.velocityY = 0;
        }

        // Controla el movimiento del segundo jugador (si está habilitado)
        if (player2 != null) {
            if (key == KeyEvent.VK_W && player2.velocityY == 0) { // Movimiento hacia arriba
                player2.velocityX = 0;
                player2.velocityY = -1;
            }
            if (key == KeyEvent.VK_S && player2.velocityY == 0) { // Movimiento hacia abajo
                player2.velocityX = 0;
                player2.velocityY = 1;
            }
            if (key == KeyEvent.VK_A && player2.velocityX == 0) { // Movimiento hacia la izquierda
                player2.velocityX = -1;
                player2.velocityY = 0;
            }
            if (key == KeyEvent.VK_D && player2.velocityX == 0) { // Movimiento hacia la derecha
                player2.velocityX = 1;
                player2.velocityY = 0;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Método vacío (sin implementación) para manejar cuando se libera una tecla
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Método vacío (sin implementación) para manejar cuando se escribe un carácter
    }

    public void restartGame() {
        // Reinicia el estado del juego
        player1 = new Player(10, 10); // Inicializa el primer jugador
        if (players == 2) {
            player2 = new Player(5, 5); // Inicializa el segundo jugador si hay dos
        }
        food = new Tile(10, 10); // Posición inicial de la comida
        placeFood(); // Coloca la comida en un lugar aleatorio válido
        gameOver = false; // Reinicia el estado de "fin del juego"
        repaint(); // Redibuja el tablero inicial
        gameLoop.start(); // Reinicia el bucle del juego
    }
}