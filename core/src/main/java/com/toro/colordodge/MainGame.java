package com.toro.colordodge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.Random;

/**
 * Clase que representa un bloque en el juego
 * Puede ser verde (puntos) o blanco (enemigo)
 */
class Block {
    Rectangle rect;  // Posición y tamaño del bloque
    Color color;     // Color del bloque (verde o blanco)

    Block(float x, float y, float width, float height, Color color) {
        rect = new Rectangle(x, y, width, height);
        this.color = color;
    }
}

/**
 * Clase principal del juego Color Dodge
 * Extiende ApplicationAdapter de LibGDX para crear el juego
 */
public class MainGame extends ApplicationAdapter {

    // === COMPONENTES DE RENDERIZADO ===
    private ShapeRenderer shapeRenderer;  // Para dibujar formas geométricas
    private SpriteBatch batch;            // Para dibujar imágenes y texto
    private BitmapFont font;              // Fuente para el texto del score
    private BitmapFont fontShadow;        // Fuente para la sombra del score

    // === TEXTURAS (IMÁGENES) ===
    private Texture textureEmpezar;       // Imagen de pantalla de inicio
    private Texture texturePerdiste;      // Imagen de pantalla de "perdiste"

    // === DIMENSIONES DE PANTALLA ===
    private float screenWidth;
    private float screenHeight;

    // === JUGADOR (BOLA) ===
    private float ballX, ballY;           // Posición de la bola
    private float ballRadius = 40f;       // Radio de la bola
    private float speed = 5f;             // Velocidad de movimiento horizontal

    // === BARRA (PISTA) ===
    private float barHeight = 30f;        // Altura de la barra
    private float barWidth;                // Ancho de la barra (se calcula)
    private float barX;                    // Posición X de la barra

    // === BLOQUES ===
    private ArrayList<Block> blocks;      // Lista de bloques en pantalla
    private float blockWidth = 80;        // Ancho de cada bloque
    private float blockHeight = 80;       // Alto de cada bloque
    private float blockSpeed = 7.5f;      // Velocidad de caída de los bloques

    // === GENERACIÓN DE BLOQUES ===
    private Random random;                 // Generador de números aleatorios
    private float spawnTimer = 0;          // Contador de tiempo para generar bloques
    private float spawnInterval = 0.8f;    // Intervalo entre generación de bloques (segundos)

    // === ESTADO DEL JUEGO ===
    private boolean gameStarted = false;  // Indica si el juego ha comenzado
    private boolean gameLost = false;      // Indica si el jugador perdió
    private int score = 0;                 // Puntuación del jugador

    /**
     * Método llamado una vez al iniciar el juego
     * Aquí se inicializan todos los componentes necesarios
     */
    @Override
    public void create() {
        // Obtener dimensiones de la pantalla
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        // Inicializar componentes de renderizado
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        // Configurar barra (pista donde se mueve la bola)
        barWidth = screenWidth * 0.8f;  // 80% del ancho de pantalla
        barX = (screenWidth - barWidth) / 2;  // Centrar la barra

        // Posición inicial de la bola (centro horizontal, 35% desde arriba)
        ballX = screenWidth / 2;
        ballY = screenHeight * 0.35f;

        // Inicializar lista de bloques y generador aleatorio
        blocks = new ArrayList<>();
        random = new Random();

        // Configurar fuente para el score
        font = new BitmapFont();
        font.getData().setScale(7f);  // Tamaño grande
        font.setColor(Color.WHITE);

        // Configurar sombra del score
        fontShadow = new BitmapFont();
        fontShadow.getData().setScale(7f);
        fontShadow.setColor(new Color(0, 0, 0, 0.5f));  // Negro semitransparente

        // Cargar imágenes de las pantallas
        textureEmpezar = new Texture(Gdx.files.internal("empezar.png"));
        texturePerdiste = new Texture(Gdx.files.internal("perdiste.png"));
    }

    /**
     * Método llamado en cada frame (60 veces por segundo aproximadamente)
     * Aquí se actualiza la lógica del juego y se dibuja todo
     */
    @Override
    public void render() {
        // Limpiar pantalla con color de fondo oscuro
        Gdx.gl.glClearColor(0.11f, 0.13f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // === PANTALLA DE INICIO O "PERDISTE" ===
        if (!gameStarted) {
            batch.begin();

            // Seleccionar imagen según el estado
            Texture currentTexture = gameLost ? texturePerdiste : textureEmpezar;

            // Calcular escala para que la imagen quepa en pantalla
            float imgWidth = currentTexture.getWidth();
            float imgHeight = currentTexture.getHeight();
            float scaleX = screenWidth / imgWidth;
            float scaleY = screenHeight / imgHeight;
            float scale = Math.min(scaleX, scaleY) * 0.95f;  // 95% para margen

            // Calcular posición centrada
            float scaledWidth = imgWidth * scale;
            float scaledHeight = imgHeight * scale;
            float x = (screenWidth - scaledWidth) / 2;
            float y = (screenHeight - scaledHeight) / 2;

            // Dibujar imagen
            batch.draw(currentTexture, x, y, scaledWidth, scaledHeight);
            batch.end();

            // Esperar toque de pantalla para iniciar
            if (Gdx.input.justTouched()) {
                gameStarted = true;
                gameLost = false;
            }
            return;  // No ejecutar el resto del código hasta que empiece
        }

        // === MOVIMIENTO DE LA BOLA ===
        // Controles con teclado (para probar en desktop)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) ballX -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) ballX += speed;

        // Controles con acelerómetro (en dispositivos Android)
        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android) {
            float accelX = Gdx.input.getAccelerometerX();
            ballX -= accelX * 2;  // Ajustar sensibilidad
        }

        // Limitar movimiento dentro de la barra
        float leftLimit = barX + ballRadius;
        float rightLimit = barX + barWidth - ballRadius;
        if (ballX < leftLimit) ballX = leftLimit;
        if (ballX > rightLimit) ballX = rightLimit;

        // === DIBUJAR ELEMENTOS DEL JUEGO ===
        // Dibujar barra (pista)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.3f));  // Negro semitransparente
        shapeRenderer.rect(barX, ballY - barHeight / 2, barWidth, barHeight);
        shapeRenderer.end();

        // Dibujar bola
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("00E3A4"));  // Verde menta
        shapeRenderer.circle(ballX, ballY, ballRadius);
        shapeRenderer.end();

        // Dibujar y mover bloques
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            // Mover bloque hacia abajo
            block.rect.y -= blockSpeed;

            // Dibujar bloque
            shapeRenderer.setColor(block.color);
            shapeRenderer.rect(block.rect.x, block.rect.y, block.rect.width, block.rect.height);

            // Eliminar bloques que salen de pantalla
            if (block.rect.y + block.rect.height < 0) {
                blocks.remove(i);
                i--;
            }
        }
        shapeRenderer.end();

        // Mostrar score en título de ventana (solo desktop)
        Gdx.graphics.setTitle("Score: " + score);

        // === DETECCIÓN DE COLISIONES ===
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            // Crear rectángulo que representa la bola para detectar colisión
            Rectangle ballRect = new Rectangle(ballX - ballRadius, ballY - ballRadius,
                                                ballRadius * 2, ballRadius * 2);

            if (block.rect.overlaps(ballRect)) {
                if (block.color.equals(Color.WHITE)) {
                    // Colisión con bloque blanco (enemigo) → Perder
                    score = 0;
                    blocks.clear();
                    ballX = screenWidth / 2;
                    gameStarted = false;
                    gameLost = true;
                    break;
                } else {
                    // Colisión con bloque verde → Sumar puntos
                    score++;
                    blockSpeed += 0.2f;  // Aumentar dificultad
                    blocks.remove(i);
                    i--;
                }
            }
        }

        // === GENERACIÓN DE BLOQUES ===
        spawnTimer += Gdx.graphics.getDeltaTime();  // DeltaTime = tiempo entre frames
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;

            // Generar entre 1 y 2 bloques aleatorios
            int blockCount = 1 + random.nextInt(2);

            for (int i = 0; i < blockCount; i++) {
                // 15% de probabilidad de bloque verde, 85% de bloque blanco
                Color blockColor = random.nextFloat() < 0.15f ?
                    Color.valueOf("00E3A4") : Color.WHITE;

                // Crear bloque en posición aleatoria en la parte superior
                blocks.add(new Block(
                    random.nextInt((int) (screenWidth - blockWidth)),
                    screenHeight,  // Aparece arriba de la pantalla
                    blockWidth,
                    blockHeight,
                    blockColor
                ));
            }
        }

        // === DIBUJAR SCORE ===
        batch.begin();
        String scoreText = String.valueOf(score);
        float scoreWidth = font.getRegion().getRegionWidth() * 0.5f;

        // Dibujar sombra del score
        fontShadow.draw(batch, scoreText, screenWidth / 2 - scoreWidth + 3, ballY - 80 - 3);

        // Dibujar score principal
        font.draw(batch, scoreText, screenWidth / 2 - scoreWidth, ballY - 80);
        batch.end();
    }

    /**
     * Método llamado al cerrar el juego
     * Libera los recursos para evitar fugas de memoria
     */
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        fontShadow.dispose();
        textureEmpezar.dispose();
        texturePerdiste.dispose();
    }
}
