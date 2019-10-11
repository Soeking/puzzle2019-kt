package com.puzzle.dcs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.*
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.google.gson.Gson
import com.badlogic.gdx.physics.box2d.FixtureDef
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.utils.Align
import java.nio.file.attribute.GroupPrincipal
import kotlin.math.cos
import kotlin.math.sin


class PlayScreen(private val game: Core, private val fileName: String) : Screen {
    private val camera: OrthographicCamera
    private val spriteBatch = SpriteBatch()
    private val file: FileHandle
    private val json = Gson()
    private lateinit var stageData: StageData
    private val gridSize = 5.0f//Gdx.graphics.width / 10f
    private val halfGrid = gridSize / 2.0f
    private val gridSize2 = Gdx.graphics.width / 10.0f
    private val halfGrid2 = gridSize2 / 2.0f
    private val world: World
    private val renderer: Box2DDebugRenderer
    private val wallSprite: Sprite
    private val squareSprite: Sprite
    private val triangleSprite: Sprite
    private val triangleSprites = mutableListOf<Sprite>()
    private val ladderSprite: Sprite
    private val playerSprite: Sprite
    private val goalSprite: Sprite
    private val button: Array<ImageButton>
    private val playerDef = BodyDef()
    private val dynamicDef = BodyDef()
    private val staticDef = BodyDef()
    private val wallBodies = mutableListOf<Body>()
    private val squareBodies = mutableListOf<Body>()
    private val triangleBodies = mutableListOf<Body>()
    private val ladderBodies = mutableListOf<Body>()
    private val playerBody: Body
    private val goalBody: Body
    private val circleShape: CircleShape
    private val boxShape: PolygonShape
    private val ladderShape: PolygonShape
    private val triangleShape: PolygonShape
    private val goalShape: PolygonShape
    private val playerFixtureDef = FixtureDef()
    private val squareFixtureDef = FixtureDef()
    private val ladderFixtureDef = FixtureDef()
    private val triangleFixtureDef = FixtureDef()
    private val goalFixtureDef = FixtureDef()
    private val playerFixture: Fixture
    private var stage: Stage

    private val topList = arrayOf(
            Vector2(halfGrid, halfGrid),
            Vector2(-halfGrid, halfGrid),
            Vector2(-halfGrid, -halfGrid),
            Vector2(halfGrid, -halfGrid)
    )
    private val left = 0
    private val up = 1
    private val right = 2
    private val down = 3

    private val fontGenerator: FreeTypeFontGenerator
    private val bitmapFont: BitmapFont

    //    アニメーション関連
    private val clearStage : Stage
    private val clearGroup: Group
    private val actions: Action
    private val label: Label
    private val container : Container<Label>
    private val clearFont: BitmapFont
    private var flg = true

    init {
        Box2D.init()
        camera = OrthographicCamera(50.0f, 50.0f / Gdx.graphics.width.toFloat() * Gdx.graphics.height.toFloat())
        camera.translate(25.0f, 25.0f / Gdx.graphics.width.toFloat() * Gdx.graphics.height)
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
        // moveArrow = Sprite(Texture(Gdx.files.internal("images/Arrow.png")))

        wallSprite.setOrigin(0f, 0f)
        wallSprite.setScale(gridSize2 / wallSprite.width)
        squareSprite.setOrigin(0f, 0f)
        squareSprite.setScale(gridSize2 / squareSprite.width)
        triangleSprite.setOrigin(0f, 0f)
        triangleSprite.setScale(gridSize2 / triangleSprite.width)
        repeat(4) { triangleSprites.add(triangleSprite) }
        ladderSprite.setOrigin(0f, 0f)
        ladderSprite.setScale(gridSize2 / ladderSprite.width)
        playerSprite.setOrigin(0f, 0f)
        playerSprite.setScale(gridSize2 / playerSprite.width / 1.5f)
        goalSprite.setOrigin(0f, 0f)
        goalSprite.setScale(gridSize2 / goalSprite.width)


        playerDef.type = BodyDef.BodyType.DynamicBody
        dynamicDef.type = BodyDef.BodyType.DynamicBody
        staticDef.type = BodyDef.BodyType.StaticBody
        dynamicDef.gravityScale = 0f

        circleShape = CircleShape()
        circleShape.radius = gridSize / 3f
        boxShape = PolygonShape()
        boxShape.setAsBox(halfGrid, halfGrid)
        ladderShape = PolygonShape()
        ladderShape.setAsBox(halfGrid, halfGrid)
        triangleShape = PolygonShape()
        goalShape = PolygonShape()
        goalShape.set(
                arrayOf(
                        Vector2(halfGrid / 2, halfGrid),
                        Vector2(-halfGrid / 2, halfGrid),
                        Vector2(-halfGrid / 2, -halfGrid),
                        Vector2(halfGrid / 2, -halfGrid)
                )
        )
        playerFixtureDef.shape = circleShape
        playerFixtureDef.density = 1.0f // 仮    //密度
        playerFixtureDef.friction = 1.0f         //摩擦
        playerFixtureDef.restitution = 0.6f     //返還
        squareFixtureDef.shape = boxShape
        squareFixtureDef.friction = 1.0f
        squareFixtureDef.restitution = 0.3f
        ladderFixtureDef.shape = ladderShape
        ladderFixtureDef.isSensor = true
        triangleFixtureDef.shape = triangleShape
        triangleFixtureDef.friction = 1.0f
        triangleFixtureDef.restitution = 0.3f
        goalFixtureDef.shape = goalShape

        if (file.exists()) {
            stageData = json.fromJson(file.readString(), StageData::class.java)
        } else {

        }

        stageData.wall.forEach {
            it.x *= gridSize
            it.y *= gridSize
            staticDef.position.set(it.x, it.y)
            val body = world.createBody(staticDef)
            body.createFixture(squareFixtureDef)
            body.userData = it
            wallBodies.add(body)
            val b = world.createBody(dynamicDef)
            b.createFixture(squareFixtureDef)
        }
        stageData.square.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(squareFixtureDef)
            squareBodies.add(body)
        }
        stageData.triangle.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            triangleShape.set(createTriangleShape(it.rotate))
            triangleFixtureDef.shape = triangleShape
            body.createFixture(triangleFixtureDef)
            triangleBodies.add(body)
        }
        stageData.ladder.forEach {
            it.x *= gridSize
            it.y *= gridSize
            dynamicDef.position.set(it.x, it.y)
            dynamicDef.position.set(it.x, it.y)
            val body = world.createBody(dynamicDef)
            body.userData = it
            body.createFixture(ladderFixtureDef)
            ladderBodies.add(body)
        }
        stageData.start.let {
            it.x *= gridSize
            it.y *= gridSize
            playerDef.position.set(it.x, it.y + 2)
            playerBody = world.createBody(playerDef)
            playerFixture = playerBody.createFixture(playerFixtureDef)
            playerBody.resetMassData()
            playerBody.userData = it
        }
        stageData.goal.let {
            it.x *= gridSize
            it.y *= gridSize
            staticDef.position.set(it.x, it.y)
            goalBody = world.createBody(staticDef)
            goalBody.userData = it
            goalBody.createFixture(goalFixtureDef)
        }

        stage = Stage()

        //button[0].setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        //button[0].setScale(gridSize / goalSprite.width)
        squareBodies.filter { (it.userData as Square).gravityID == 2 }.forEach {
            it.setLinearVelocity(0f, -12f)
        }
        triangleBodies.filter { (it.userData as Triangle).gravityID == 2 }.forEach {
            it.setLinearVelocity(0f, -12f)
        }

        //フォント生成
        var file = Gdx.files.internal("fonts/Roboto-Black.ttf")
        fontGenerator = FreeTypeFontGenerator(file)
        var param = FreeTypeFontGenerator.FreeTypeFontParameter()
        param.size = 25
        param.color = Color.RED
        param.incremental = true
        bitmapFont = fontGenerator.generateFont(param)
        //bitmapFont = BitmapFont()
        //bitmapFont.color = Color.WHITE
        //bitmapFont.data.setScale(10f)


//        アニメーション関連
        clearStage = Stage()
        clearGroup = Group()
        clearGroup.setPosition(0f, 0f)
        clearStage.addActor(clearGroup)
        actions = Actions.sequence(
                Actions.moveTo(0f, 100f, 0.5f, Interpolation.pow2In),
                Actions.moveTo(0f, 0f, 0.5f, Interpolation.pow2In),
                Actions.moveTo(100f, 0f, 0.5f, Interpolation.pow2In),
                Actions.moveTo(100f, 100f, 0.5f, Interpolation.pow2In),
                Actions.scaleTo(300f, 300f, 2.0f)/*,
                Actions.run(object : Runnable {
                    override fun run() {
                        Gdx.app.log("Clear", "Finishes")
                        flg = true
                    }
                })*/
        )
        clearFont = fontGenerator.generateFont(param)
        label = Label("Clear", Label.LabelStyle(clearFont, Color(1f, 0f, 0f, 0.5f)))
//        label.setText("Clear!")
//        label.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
//        label.setFontScale(2f)
//        label.setAlignment(Align.center)
        container = Container(label)
        container.isTransform = true
        container.setSize(0f, 0f)
        container.setOrigin(container.getWidth() / 2, container.getHeight() / 2)
        container.setPosition(Gdx.graphics.width / 2f, Gdx.graphics.height / 2f)
        container.setScale(3f)
        clearGroup.addActor(container)


        button = arrayOf(
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow1.png"))))),
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow2.png"))))),
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow3.png"))))),
                ImageButton(TextureRegionDrawable(TextureRegion(Texture(Gdx.files.internal("images/Arrow4.png")))))
        )
        repeat(4) {
            button[it].image.setScale(Gdx.graphics.width / 10.0f / button[it].width)
            button[it].image.setColor(button[it].image.color.r, button[it].image.color.g, button[it].image.color.b, 0.5f)
            button[it].setScale(Gdx.graphics.width / 10.0f / button[it].width / 1f)
            //button[i].setScale(10f)
            //button[i].setOrigin(button[i].width / 2.0f, button[i].height / 2.0f)
            button[it].setOrigin(0.0f, 0.0f)
            //button[i].setPosition(Gdx.graphics.width / 12.0f * 3.0f + Gdx.graphics.width / 6.0f * (-Math.cos(Math.PI * i / 2.0).toFloat()), Gdx.graphics.height / 8.0f * 3.0f + Gdx.graphics.height / 4.0f * (Math.sin(Math.PI * i / 2.0)).toFloat())
            button[it].setPosition(
                    Gdx.graphics.width / 10.0f / 3.0f * 2.0f + Gdx.graphics.width / 10.0f / 3.0f * 2.0f * (-cos(Math.PI * it / 2.0).toFloat()),
                    Gdx.graphics.width / 10.0f / 3.0f * 2.0f + Gdx.graphics.width / 10.0f / 3.0f * 2.0f * (sin(Math.PI * it / 2.0).toFloat())
            )
            button[it].color.set(Color.BLACK)
            stage.addActor(button[it])
            //button[i].rotation(0.0f)
            Gdx.app.log("button", "${button[it].x},${button[it].y},${button[it].width},${button[it].height}")
        }
        Gdx.input.inputProcessor = stage

        circleShape.dispose()
        boxShape.dispose()
        ladderShape.dispose()
        triangleShape.dispose()
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

    private fun createTriangleShape(rotate: Int): Array<Vector2> {
        val list = mutableListOf<Vector2>()
        list.addAll(topList.filter { it != topList[rotate] })
        return list.toTypedArray()
    }

    override fun render(delta: Float) {
        //button()

        Gdx.gl.glClearColor(0.1f, 0.4f, 0.8f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        world.contactList.forEach {
            collisionAction(it.fixtureA.body, it.fixtureB.body)
        }

        spriteBatch.begin()
        bitmapFont.draw(spriteBatch, "(${playerBody.position.x.toInt()}, ${playerBody.position.y.toInt()})\n(${playerBody.linearVelocity.x.toInt()}, ${playerBody.linearVelocity.y.toInt()})", Gdx.graphics.width - 150.0f, Gdx.graphics.height - 20.0f)
        //drawSprites()
        spriteBatch.end()
        if (button[left].isPressed && flg) {
            gameClear()
            stage.addActor(clearGroup)
        }
        drawUI()
        button()

        camera.update()
        world.step(Gdx.graphics.deltaTime, 1, 0)
        renderer.render(world, camera.combined)
    }

    private val speed = 1.0f

    private fun collisionAction(a: Body, b: Body) {
        if (a == playerBody) {

        } else if (b == playerBody) {

        } else {

        }
    }

    private var no = false

    private fun button() {
        //Gdx.app.log("TEST", "A")
        //Gdx.app.log("Gravity", "${playerBody.linearVelocity.x}, ${playerBody.linearVelocity.y}")

        var temp = 0

        repeat(4) {
            if (button[it].isPressed) {
                no = true
                temp++
                //Gdx.app.log("pressed", "${i} , ${Gdx.graphics.deltaTime}")
                when (it) {
                    left -> {
                        //playerBody.position.set(playerBody.position.x - SPEED * Gdx.graphics.deltaTime, playerBody.position.y)
                        //playerBody.setLinearVelocity(-SPEED, playerBody.linearVelocity.y)
                        //playerBody.setLinearVelocity(playerBody.linearVelocity.add(Vector2(-SPEED, 0.0f)))
                        //playerBody.applyForceToCenter(Vector2(-speed, 0.0f), true)
                        playerBody.applyLinearImpulse(-speed, 0.0f, playerBody.position.x, playerBody.position.y, true)
                        //playerBody.linearVelocity.x = -SPEED

                        /*
                        // compute the aiming direction
                        //var direction = Vector2(diff.x / dist, diff.y / dist)

                        // get the current missile velocity because we will apply a force to compensate this.
                        var currentVelocity = playerBody.linearVelocity

                        // the missile ideal velocity is the direction to the target multiplied by the max speed
                        var desireVelocity = Vector2 (1.0f * speed, 0.0f * speed);

                        // compensate the current missile velocity by the desired velocity, based on the control factor

                        var finalVelocity = Vector2(desireVelocity.x - currentVelocity.x, desireVelocity.y - currentVelocity.y);

                        // transform our velocity into an impulse (get rid of the time and mass factor)
                        var temp = (playerBody.mass / 1.0f)

                        var finalForce = Vector2 (finalVelocity.x * temp, finalVelocity.y * temp);

                        playerBody.applyForce(finalForce, playerBody.worldCenter, true);
                        */
                    }
                    up -> {
                        //playerBody.position.set(playerBody.position.x, playerBody.position.y + SPEED * Gdx.graphics.deltaTime)
                        //playerBody.setLinearVelocity(playerBody.linearVelocity.x, SPEED)
                        //playerBody.applyForceToCenter(0.0f, speed, true)
                        playerBody.applyLinearImpulse(0.0f, speed, playerBody.position.x, playerBody.position.y, true)
                        //playerBody.linearVelocity.y = SPEED
                    }
                    right -> {
                        //playerBody.position.set(playerBody.position.x + SPEED * Gdx.graphics.deltaTime, playerBody.position.y)
                        //playerBody.setLinearVelocity(SPEED, playerBody.linearVelocity.y)
                        //playerBody.applyForceToCenter(speed, 0.0f, true)
                        playerBody.applyLinearImpulse(speed, 0.0f, playerBody.position.x, playerBody.position.y, true)
                        //playerBody.linearVelocity.set(SPEED, playerBody.linearVelocity.y)
                        //playerBody.linearVelocity.x = SPEED
                    }
                    down -> {
                        //playerBody.position.set(playerBody.position.x, playerBody.position.y - SPEED * Gdx.graphics.deltaTime)
                        //playerBody.setLinearVelocity(playerBody.linearVelocity.x, -SPEED)
                        //playerBody.applyForceToCenter(0.0f, -speed, true)
                        playerBody.applyLinearImpulse(0.0f, -speed, playerBody.position.x, playerBody.position.y, true)
                        //playerBody.linearVelocity.y = -SPEED
                    }
                }
            }
        }
        if (temp == 0 && no) {
            no = false
            //playerBody.setLinearVelocity(0.0f, 0.0f)
        }

        /*if (button[Left].isPressed) {
            Gdx.app.log("TEST", "${playerBody.position.y}, ${playerBody.linearVelocity.y}")
        }*/
        //Gdx.app.log("TEST", "PO:${playerBody.position.y}, SPE:${playerBody.linearVelocity.y}")
    }

    private fun drawUI() {
        /* val sprite = moveArrow
        sprite.setPosition(Gdx.graphics.width / 15.0f, Gdx.graphics.height / 10.0f)
        sprite.setColor(sprite.color.r, sprite.color.g, sprite.color.b, 0.3f)
        sprite.draw(spriteBatch) */
        stage.act(Gdx.graphics.deltaTime)
        stage.draw()
        clearStage.act(Gdx.graphics.deltaTime)
        clearStage.draw()
    }

    private fun drawSprites() {
        wallBodies.forEach {
            val sprite = wallSprite
            sprite.setPosition(it.position.x * gridSize2 / gridSize - halfGrid2, it.position.y * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
        squareBodies.forEach {
            val sprite = squareSprite
            sprite.setPosition(it.position.x * gridSize2 / gridSize - halfGrid2, it.position.y * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
        triangleBodies.forEach {
            val sprite = triangleSprites[(it.userData as Triangle).rotate]
            sprite.setPosition(it.position.x * gridSize2 / gridSize - halfGrid2, it.position.y * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
        ladderBodies.forEach {
            val sprite = ladderSprite
            sprite.setPosition(it.position.x * gridSize2 / gridSize - halfGrid2, it.position.y * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
        playerBody.let {
            val sprite = playerSprite
            sprite.setPosition(it.position.x * gridSize2 / gridSize - gridSize2 / 3f, it.position.y * gridSize2 / gridSize - gridSize2 / 3f)
            sprite.draw(spriteBatch)
        }
        goalBody.let {
            val sprite = goalSprite
            sprite.setPosition(it.position.x * gridSize2 / gridSize - halfGrid2, it.position.y * gridSize2 / gridSize - halfGrid2)
            sprite.draw(spriteBatch)
        }
    }

    private fun gameClear() {
        container.addAction(actions)
        flg = false
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
}