package com.toro.colordodge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.Random;


class Block {
    Rectangle rect;
    Color color;

    Block(float x, float y, float width, float height, Color color) {
        rect = new Rectangle(x, y, width, height);
        this.color = color;
    }
}




public class MainGame extends ApplicationAdapter {

    private ShapeRenderer shapeRenderer;
    private float ballX, ballY;
    private float ballRadius = 40f;
    private float speed = 5f;
    private float screenWidth, screenHeight;
    private ArrayList<Block> blocks;// Lista de cuadros
    private float blockWidth = 80, blockHeight = 80;;
    private float blockSpeed = 7.5f; // Se siente más dinámico
    private Random random;

    private float barHeight = 30f;  // altura de la barra
    private float barWidth;        // se ajustará en runtime
    private float barX; // posición izquierda de la barra

    private boolean gameStarted = false;


    private BitmapFont font;
    private BitmapFont fontShadow;
    private SpriteBatch batch;


    private int score = 0; // CONTADOR DE PUNTOS
    private float spawnTimer = 0;
    private float spawnInterval = 0.8f; // tiempo entre apariciones (0.8s) - Más bloques, más seguido
    @Override
    public void create() {
        shapeRenderer = new ShapeRenderer();

        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();

        barWidth = screenWidth * 0.8f; // 80% del ancho de la pantalla
        barX = (screenWidth - barWidth) / 2; // posición centrada de la barra

        // Posición inicial al centro
        ballX = screenWidth / 2;
        ballY = screenHeight * 0.35f; // bajar la bolita alineada a la barra
        Gdx.app.log("DEBUG", "Game Started - Acelerómetro listo");

        blocks = new ArrayList<>();
        random = new Random();

        batch = new SpriteBatch();
        font = new BitmapFont(); // fuente por defecto (blanca)
        font.getData().setScale(7f); // HUD extra grande y grueso
        font.setColor(Color.WHITE);

        fontShadow = new BitmapFont();
        fontShadow.getData().setScale(font.getData().scaleX); // mismo tamaño
        fontShadow.setColor(new Color(0, 0, 0, 0.5f)); // sombra negra con transparencia


    }

    @Override
    public void render() {
        // Fondo oscuro (#1E2233)
        Gdx.gl.glClearColor(0.11f, 0.13f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        // Mostrar mensaje de inicio
        if (!gameStarted) {
            batch.begin();

            font.getData().setScale(4.5f);
            fontShadow.getData().setScale(4.5f);

            String title = "¡COLOR DODGE!";
            float titleWidth = font.getRegion().getRegionWidth() * 0.5f;

            // 🔹 SOMBRA
            fontShadow.draw(batch, title, screenWidth / 2 - titleWidth + 4, screenHeight * 0.6f - 4);
            // 🔹 TEXTO PRINCIPAL
            font.draw(batch, title, screenWidth / 2 - titleWidth, screenHeight * 0.6f);

            font.getData().setScale(3f);
            fontShadow.getData().setScale(3f);
            String subtitle = "Toca para empezar - Inclina para mover";

            // 🔹 SOMBRA SUBTÍTULO
            fontShadow.draw(batch, subtitle, screenWidth * 0.2f + 3, screenHeight * 0.5f - 3);
            // 🔹 TEXTO PRINCIPAL SUBTÍTULO
            font.draw(batch, subtitle, screenWidth * 0.2f, screenHeight * 0.5f);

            batch.end();

            // Espera a que el jugador toque la pantalla
            if (Gdx.input.justTouched()) {
                gameStarted = true;
            }
            return; // Detiene lógica hasta que empiece
        }

        // Movimiento con FLECHAS (para probar rápido en PC)
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) ballX -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) ballX += speed;

        // Movimiento con ACELERÓMETRO
        if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Android) {
            float accelX = Gdx.input.getAccelerometerX(); // inclinación
            ballX -= accelX * 2; // Multiplicamos sensibilidad
        }

        // Limitar dentro de la barra
        float leftLimit = barX + ballRadius;
        float rightLimit = barX + barWidth - ballRadius;

        if (ballX < leftLimit) ballX = leftLimit;
        if (ballX > rightLimit) ballX = rightLimit;

        // Dibujar la barra horizontal detrás de la bola (tipo pista)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(new Color(0, 0, 0, 0.3f)); // negro con transparencia
        shapeRenderer.rect(
            (screenWidth - barWidth) / 2,       // centrado en X
            ballY - barHeight / 2,             // centrado debajo de la bola
            barWidth,
            barHeight
        );
        shapeRenderer.end();

        // Dibujar bolita color verde
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.valueOf("00E3A4")); // Verde menta
        shapeRenderer.circle(ballX, ballY, ballRadius);
        shapeRenderer.end();

        // Dibujar y mover cada bloque
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            // Mover hacia abajo
            block.rect.y -= blockSpeed;

            // Dibujar bloque con su color
            shapeRenderer.setColor(block.color);
            shapeRenderer.rect(block.rect.x, block.rect.y, block.rect.width, block.rect.height);

            // Eliminar bloques que salen de pantalla
            if (block.rect.y + block.rect.height < 0) {
                blocks.remove(i);
                i--;
            }
        }
        shapeRenderer.end();
        // Dibujar SCORE tipo "17"
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        Gdx.gl.glLineWidth(3);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.end();

        Gdx.graphics.setTitle("Score: " + score); // También muestra score en título (Desktop debug)


        // DETECCIÓN DE COLISIONES
        for (int i = 0; i < blocks.size(); i++) {
            Block block = blocks.get(i);

            // Calcular colisión circular con rectángulo
            if (block.rect.overlaps(new Rectangle(ballX - ballRadius, ballY - ballRadius, ballRadius * 2, ballRadius * 2))) {

                if (block.color.equals(Color.WHITE)) {
                    // 🚨 Colisión con enemigo → Reiniciar juego
                    score = 0;
                    blocks.clear();
                    ballX = screenWidth / 2;
                    gameStarted = false; // Vuelve a pantalla de inicio
                    break;
                }
                else {
                    // ✅ Colisión con bloque verde → sumar y eliminar
                    score++;
                    blockSpeed += 0.2f; // cada vez que sumas, el juego se pone más difícil
                    blocks.remove(i);
                    i--;
                }
            }
        }



        // Timer para generar bloques
        spawnTimer += Gdx.graphics.getDeltaTime();
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;

            // GENERAR ENTRE 1 Y 2 BLOQUES ALEATORIOS
            int blockCount = 1 + random.nextInt(2); // genera 1 o 2 bloques

            for (int i = 0; i < blockCount; i++) {
                Color blockColor = random.nextFloat() < 0.15f ? Color.valueOf("00E3A4") : Color.WHITE;

                blocks.add(new Block(
                    random.nextInt((int) (screenWidth - blockWidth)),
                    screenHeight,
                    blockWidth,
                    blockHeight,
                    blockColor
                ));
            }
        }


        batch.begin();
        String scoreText = String.valueOf(score);
        float scoreWidth = font.getRegion().getRegionWidth() * 0.5f;

        // 🔹 DIBUJAR SOMBRA PRIMERO
        fontShadow.draw(batch, scoreText, screenWidth / 2 - scoreWidth + 3, ballY - 80 - 3);

        // 🔹 DIBUJAR TEXTO PRINCIPAL
        font.draw(batch, scoreText, screenWidth / 2 - scoreWidth, ballY - 80);
        batch.end();



    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        fontShadow.dispose();
    }
}
