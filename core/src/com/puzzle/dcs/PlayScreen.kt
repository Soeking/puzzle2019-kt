package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.google.gson.Gson

class PlayScreen(private val game: Core, private val fileName: String) : Screen, InputProcessor {
    private val camera: OrthographicCamera
    private val spriteBatch = SpriteBatch()
    private val file: FileHandle
    private val json = Gson()
    private lateinit var stageData: StageData
    private val gridSize = Gdx.graphics.width / 10f
    private val halfGrid = gridSize / 2f
    private val world: World
    private val renderer: Box2DDebugRenderer
    private val wallSprite: Sprite
    private val squareSprite: Sprite
    private val triangleSprite: Sprite
    private val ladderSprite: Sprite
    private val playerSprite: Sprite
    private val goalSprite: Sprite
    private val dynamicDef = BodyDef()
    private val staticDef = BodyDef()
    private val kinematicDef = BodyDef()
    private val wallBodies = mutableListOf<Body>()
    private val squareBodies = mutableListOf<Body>()
    private val triangleBodies = mutableListOf<Body>()
    private val ladderBodies = mutableListOf<Body>()
    private val playerBody: Body
    private val goalBody: Body
    private val circleShape: CircleShape
    private val boxShape: PolygonShape
    private val playerFixtureDef = FixtureDef()
    private val wallFixtureDef = FixtureDef()
    private val playerFixture: Fixture

    init {
        Box2D.init()
        camera = OrthographicCamera()
        world = World(Vector2(0f, -8f), true)
        renderer = Box2DDebugRenderer()
        createCollision()

        file = Gdx.files.internal("stages/$fileName")
        wallSprite = Sprite(Texture(Gdx.files.internal("images/wall.png")))
        squareSprite = Sprite(Texture(Gdx.files.internal("images/square.png")))
        triangleSprite = Sprite(Texture(Gdx.files.internal("images/triangle.png")))
        ladderSprite = Sprite(Texture(Gdx.files.internal("images/ladder.png")))
        playerSprite = Sprite(Texture(Gdx.files.internal("images/ball.png")))
        goalSprite = Sprite(Texture(Gdx.files.internal("images/goal.png")))

        wallSprite.setOrigin(0f, 0f)
        wallSprite.setScale(gridSize / wallSprite.width)
        squareSprite.setOrigin(0f, 0f)
        squareSprite.setScale(gridSize / squareSprite.width)
        triangleSprite.setOrigin(0f, 0f)
        triangleSprite.setScale(gridSize / triangleSprite.width)
        ladderSprite.setOrigin(0f, 0f)
        ladderSprite.setScale(gridSize / ladderSprite.width)
        playerSprite.setOrigin(0f, 0f)
        playerSprite.setScale(gridSize / playerSprite.width / 1.5f)
        goalSprite.setOrigin(0f, 0f)
        goalSprite.setScale(gridSize / goalSprite.width)

        dynamicDef.type = BodyDef.BodyType.DynamicBody
        dynamicDef.position.set(0f, 0f)
        staticDef.type = BodyDef.BodyType.StaticBody
        staticDef.position.set(0f, 0f)
        kinematicDef.type = BodyDef.BodyType.KinematicBody
        kinematicDef.position.set(0f, 0f)

        circleShape = CircleShape()
        circleShape.radius = gridSize / 3f
        boxShape = PolygonShape()
        boxShape.setAsBox(gridSize / 2f, gridSize / 2f)
        playerFixtureDef.shape = circleShape
        playerFixtureDef.isSensor = true
        playerFixtureDef.friction = 0.5f
        playerFixtureDef.restitution = 0.1f
        wallFixtureDef.shape = boxShape
        wallFixtureDef.isSensor = true
        wallFixtureDef.friction = 1f
        wallFixtureDef.restitution = 0f

        if (file.exists()) {
            stageData = json.fromJson(file.readString(), StageData::class.java)
        } else {

        }

        stageData.wall.forEach {
            it.x *= gridSize
            it.y *= gridSize
            val body = world.createBody(staticDef)
            val sprite = wallSprite
            //sprite.setPosition(it.x, it.y)
            body.userData = sprite
            body.setTransform(it.x, it.y, 0f)
            body.createFixture(wallFixtureDef)
            wallBodies.add(body)
        }
        stageData.square.forEach {
            it.x *= gridSize
            it.y *= gridSize
            val body = world.createBody(kinematicDef)
            val sprite = squareSprite
            //sprite.setPosition(it.x, it.y)
            body.userData = sprite
            body.setTransform(it.x, it.y, 0f)
            squareBodies.add(body)
        }
        stageData.triangle.forEach {
            it.x *= gridSize
            it.y *= gridSize
            val body = world.createBody(kinematicDef)
            val sprite = triangleSprite
            //sprite.setPosition(it.x, it.y)
            body.userData = sprite
            body.setTransform(it.x, it.y, 0f)
            triangleBodies.add(body)
        }
        stageData.ladder.forEach {
            it.x *= gridSize
            it.y *= gridSize
            val body = world.createBody(kinematicDef)
            val sprite = ladderSprite
            //sprite.setPosition(it.x, it.y)
            body.userData = sprite
            body.setTransform(it.x, it.y, 0f)
            ladderBodies.add(body)
        }
        stageData.start.let {
            it.x *= gridSize
            it.y *= gridSize
            val sprite = playerSprite
            //sprite.setPosition(it.x, it.y)
            playerBody = world.createBody(dynamicDef)
            playerBody.userData = sprite
            playerBody.setTransform(it.x, it.y + 10, 0f)
            playerFixture = playerBody.createFixture(playerFixtureDef)
        }
        stageData.goal.let {
            it.x *= gridSize
            it.y *= gridSize
            val sprite = goalSprite
            //sprite.setPosition(it.x, it.y)
            goalBody = world.createBody(staticDef)
            goalBody.userData = sprite
            goalBody.setTransform(it.x, it.y, 0f)
        }
    }

    private fun createCollision() {
        world.setContactListener(object : ContactListener {
            override fun beginContact(contact: Contact?) {

            }

            override fun endContact(contact: Contact?) {

            }

            override fun preSolve(contact: Contact?, oldManifold: Manifold?) {

            }

            override fun postSolve(contact: Contact?, impulse: ContactImpulse?) {

            }
        })
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0.1f, 0.4f, 0.8f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (world.contactCount > 0) {
            world.contactList.forEach {
                Gdx.app.log("contact", "${it.fixtureA.body.position},${it.fixtureB.body.position}")
            }
        }

        spriteBatch.begin()
        drawSprites()
        spriteBatch.end()

        camera.update()
        world.step(Gdx.graphics.deltaTime, 0, 0)
        renderer.render(world, camera.combined)
    }

    private fun drawSprites() {
        wallBodies.forEach {
            val sprite = wallSprite
            sprite.setPosition(it.position.x - halfGrid, it.position.y - halfGrid)
            sprite.draw(spriteBatch)
        }
        squareBodies.forEach {
            val sprite = squareSprite
            sprite.setPosition(it.position.x-halfGrid, it.position.y-halfGrid)
            sprite.draw(spriteBatch)
        }
        triangleBodies.forEach {
            val sprite = triangleSprite
            sprite.setPosition(it.position.x-halfGrid, it.position.y-halfGrid)
            sprite.draw(spriteBatch)
        }
        ladderBodies.forEach {
            val sprite = ladderSprite
            sprite.setPosition(it.position.x-halfGrid, it.position.y-halfGrid)
            sprite.draw(spriteBatch)
        }
        playerBody.let {
            val sprite = playerSprite
            sprite.setPosition(it.position.x - gridSize / 3f, it.position.y - gridSize / 3f)
            sprite.draw(spriteBatch)
        }
        goalBody.let {
            val sprite = goalSprite
            sprite.setPosition(it.position.x-halfGrid, it.position.y-halfGrid)
            sprite.draw(spriteBatch)
        }
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return true
    }

    override fun resize(width: Int, height: Int) {

    }

    override fun show() {

    }

    override fun hide() {

    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {

    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }
}