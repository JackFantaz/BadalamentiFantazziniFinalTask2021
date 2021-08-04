package carparking.test

import org.junit.BeforeClass
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import it.unibo.kactor.QakContext
import it.unibo.kactor.ActorBasic
import kotlinx.coroutines.channels.Channel
import org.junit.Before
import org.junit.Test
import org.junit.AfterClass
import kotlin.jvm.JvmStatic
import itunibo.planner.plannerUtil
import carparking.directionalPlanner
import org.junit.After
import org.junit.Assert.assertEquals
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class Sprint3Test {

	companion object {

		var actor: ActorBasic? = null
		var syncChannel = Channel<String>()
		var obsChannel = Channel<String>()
		var observer: CoapObserverForTesting? = null
		var started = false

		@JvmStatic
		@BeforeClass
		fun beforeAll() {
			GlobalScope.launch { it.unibo.ctxcarparking.main() }
			GlobalScope.launch {
				while (actor == null) {
					println("waiting for system startup...")
					delay(500)
					actor = QakContext.getActor("parkserviceguiactor")
				}
				delay(3000)
				syncChannel.send("starttesting")
			}
		}

		@JvmStatic
		@AfterClass
		fun afterAll() {
			runBlocking { delay(3000) }
		}

	}

	@Before
	fun beforeEach() {
		if (!started) {
			runBlocking {
				syncChannel.receive()
				started = true
			}
		}
	}

	@After
	fun afterEach() {
		runBlocking { delay(3000) }
	}

	/*@Test
	fun checkCleanSequence() {
		runBlocking {

			println("checkCleanSequence -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			assertNotMovingInTime(3000)

			println("checkCleanSequence -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			assertLocationInTime("6", "0", "N", 10000)
			assertLocationInTime("4", "3", "W", 10000)
			assertLocationInTime("0", "0", "S", 10000)
			assertNotMovingInTime(3000)

			println("checkCleanSequence -> forward exitRequest(1)")
			actor!!.forward("exitRequest", "exitRequest(1)", "parkmanagerserviceactor")
			assertLocationInTime("4", "3", "W", 10000)
			assertLocationInTime("6", "4", "S", 10000)
			assertLocationInTime("0", "0", "S", 50000)
			assertNotMovingInTime(3000)

		}
	}*/

	/*@Test
	fun checkRobustSequence() {
		runBlocking {

			assertNotMovingInTime(3000)

			for (i in 1..2) {

				println("checkRobustSequence -> forward exitRequest(1)")
				actor!!.forward("exitRequest", "exitRequest(1)", "parkmanagerserviceactor")
				assertNotMovingInTime(3000)

				println("checkRobustSequence -> forward enterRequest(0)")
				actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
				assertNotMovingInTime(3000)

				println("checkRobustSequence -> forward carEnter(1)")
				actor!!.forward("carEnter", "carEnter(1)", "parkmanagerserviceactor")
				assertLocationInTime("6", "0", "N", 10000)
				assertLocationInTime("1", "1", "E", 10000)
				assertLocationInTime("0", "0", "S", 10000)
				assertNotMovingInTime(3000)

				println("checkRobustSequence -> forward enterRequest(0)")
				actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
				assertNotMovingInTime(3000)

				println("checkRobustSequence -> forward exitRequest(1)")
				actor!!.forward("exitRequest", "exitRequest(1)", "parkmanagerserviceactor")
				assertLocationInTime("1", "1", "E", 10000)
				assertLocationInTime("6", "4", "S", 10000)
				assertLocationInTime("0", "0", "S", 50000)
				assertNotMovingInTime(3000)

			}

		}
	}*/

	/*@Test
	fun checkDoors() {
		runBlocking {

			println("checkDoors -> emit indoorOccupied(0)")
			actor!!.emit("indoorOccupied", "indoorOccupied(0)")

			println("checkDoors -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")

			println("checkDoors -> forward carEnter(1)")
			actor!!.forward("carEnter", "carEnter(1)", "parkmanagerserviceactor")

			assertNotMovingInTime(3000)

			println("checkDoors -> emit indoorCleared(0)")
			actor!!.emit("indoorCleared", "indoorCleared(0)")

			println("checkDoors -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")

			assertLocationInTime("6", "0", "N", 10000)
			assertLocationInTime("0", "0", "S", 20000)

			println("checkDoors -> emit outdoorOccupied(0)")
			actor!!.emit("outdoorOccupied", "outdoorOccupied(0)")

			println("checkDoors -> forward exitRequest(0)")
			actor!!.forward("exitRequest", "exitRequest(0)", "parkmanagerserviceactor")

			assertNotMovingInTime(3000)

			println("checkDoors -> emit outdoorCleared(0)")
			actor!!.emit("outdoorCleared", "outdoorCleared(0)")

			println("checkDoors -> forward exitRequest(0)")
			actor!!.forward("exitRequest", "exitRequest(0)", "parkmanagerserviceactor")

			assertLocationInTime("6", "4", "S", 20000)
			assertLocationInTime("0", "0", "S", 60000)

		}
	}*/

	/*@Test
	fun checkSensorsAndActuators() {
		runBlocking {

			observe("weightactor", arrayOf("indoorOccupied", "indoorCleared"))
			println("checkSensorsAndActuators -> please raise weight above threshold (default 60)")
			assertEvent("indoorOccupied(0)")
			println("checkSensorsAndActuators -> please lower weight below threshold (default 60)")
			assertEvent("indoorCleared(0)")

			observe("sonaractor", arrayOf("outdoorOccupied", "outdoorCleared"))
			println("checkSensorsAndActuators -> please bring sonar below threshold (default 40)")
			assertEvent("outdoorOccupied(0)")
			println("checkSensorsAndActuators -> please move sonar above threshold (default 40)")
			assertEvent("outdoorCleared(0)")

			println("checkSensorsAndActuators -> please set temperature to 15.0 degrees and press ENTER on console")
			print("> ")
			readLine()
			observe("thermometeractor", arrayOf("temperature"))
			assertEvent("temperature(15.0)")

			actor!!.forward("fanStart", "fanStart(0)", "fanactor")
			println("checkSensorsAndActuators -> please wait for fan to turn on and press ENTER on console")
			print("> ")
			readLine()
			actor!!.forward("fanStop", "fanStop(0)", "fanactor")
			println("checkSensorsAndActuators -> please wait for fan to turn off and press ENTER on console")
			print("> ")
			readLine()

		}
	}*/

	/*@Test
	fun checkAlarms() {
		runBlocking {

			observe("temperaturesentinelactor", arrayOf("temperatureAlarm(0)", "temperatureAlarmRevoked(0)"))
			println("checkAlarms -> emit temperature(10.0)")
			actor!!.emit("temperature", "temperature(10.0)")
			assertNoEventInTime(1000)
			println("checkAlarms -> emit temperature(40.0)")
			actor!!.emit("temperature", "temperature(40.0)")
			assertEvent("temperatureAlarm(0)")
			println("checkAlarms -> emit temperature(10.0)")
			actor!!.emit("temperature", "temperature(10.0)")
			assertEvent("temperatureAlarmRevoked(0)")

			observe("outdoorsentinelactor", arrayOf("outdoorAlarm(0)", "outdoorAlarmRevoked(0)"))
			println("checkAlarms -> emit outdoorCleared(0)")
			actor!!.emit("outdoorCleared", "outdoorCleared(0)")
			assertNoEventInTime(1000)
			println("checkAlarms -> emit outdoorOccupied(0)")
			actor!!.emit("outdoorOccupied", "outdoorOccupied(0)")
			assertNoEventInTime(4000)
			assertEvent("outdoorAlarm(0)")
			println("checkAlarms -> emit outdoorCleared(0)")
			actor!!.emit("outdoorCleared", "outdoorCleared(0)")
			assertEvent("outdoorAlarmRevoked(0)")

			observe("fanactor", arrayOf("fanStop(0)", "fanStart(0)"))
			assertEvent("fanStop(0)")
			println("checkAlarms -> forward fanAuto(auto)")
			actor!!.forward("fanAuto", "fanAuto(auto)", "parkservicestatusguiactor")
			println("checkAlarms -> emit temperatureAlarm(0)")
			actor!!.emit("temperatureAlarm", "temperatureAlarm(0)")
			assertEvent("fanStart(0)")
			println("checkAlarms -> emit temperatureAlarmRevoked(0)")
			actor!!.emit("temperatureAlarmRevoked", "temperatureAlarmRevoked(0)")
			assertEvent("fanStop(0)")
			println("checkAlarms -> forward fanAuto(manual)")
			actor!!.forward("fanAuto", "fanAuto(manual)", "parkservicestatusguiactor")
			println("checkAlarms -> emit temperatureAlarm(0)")
			actor!!.emit("temperatureAlarm", "temperatureAlarm(0)")
			assertNoEventInTime(1000)
			println("checkAlarms -> emit temperatureAlarmRevoked(0)")
			actor!!.emit("temperatureAlarmRevoked", "temperatureAlarmRevoked(0)")
			assertNoEventInTime(1000)

		}
	}
	
	@Test
	fun checkLocations() {
		runBlocking {

			println("checkLocations -> forward goto(indoor)")
			actor!!.forward("goto", "goto(indoor)", "trolleyactor")
			assertLocationInTime("6", "0", "N", 10000)
			assertNotMovingInTime(2000)
			
			println("checkLocations -> forward goto(outdoor)")
			actor!!.forward("goto", "goto(outdoor)", "trolleyactor")
			assertLocationInTime("6", "4", "S", 10000)
			assertNotMovingInTime(2000)
			
			println("checkLocations -> forward goto(parking1)")
			actor!!.forward("goto", "goto(parking1)", "trolleyactor")
			assertLocationInTime("1", "1", "E", 10000)
			assertNotMovingInTime(2000)
			
			println("checkLocations -> forward goto(parking2)")
			actor!!.forward("goto", "goto(parking2)", "trolleyactor")
			assertLocationInTime("1", "2", "E", 10000)
			assertNotMovingInTime(2000)
			
			println("checkLocations -> forward goto(parking3)")
			actor!!.forward("goto", "goto(parking3)", "trolleyactor")
			assertLocationInTime("1", "3", "E", 10000)
			assertNotMovingInTime(2000)
			
			println("checkLocations -> forward goto(parking4)")
			actor!!.forward("goto", "goto(parking4)", "trolleyactor")
			assertLocationInTime("4", "1", "W", 10000)
			assertNotMovingInTime(2000)
			
			println("checkLocations -> forward goto(parking5)")
			actor!!.forward("goto", "goto(parking5)", "trolleyactor")
			assertLocationInTime("4", "2", "W", 10000)
			assertNotMovingInTime(2000)
			
			println("checkLocations -> forward goto(parking6)")
			actor!!.forward("goto", "goto(parking6)", "trolleyactor")
			assertLocationInTime("4", "3", "W", 10000)
			assertNotMovingInTime(2000)
			
			println("checkLocations -> forward goto(home)")
			actor!!.forward("goto", "goto(home)", "trolleyactor")
			assertLocationInTime("0", "0", "S", 10000)
			assertNotMovingInTime(2000)

		}
	}*/
	
	/*@Test
	fun checkTrolleyStop() {
		runBlocking {

			println("checkTrolleyStop -> forward stopTrolley(0)")
			actor!!.forward("stopTrolley", "stopTrolley(0)", "trolleyactor")
			println("checkTrolleyStop -> forward goto(parking6)")
			actor!!.forward("goto", "goto(parking6)", "trolleyactor")
			assertNotMovingInTime(2000)
			println("checkTrolleyStop -> forward startTrolley(0)")
			actor!!.forward("startTrolley", "startTrolley(0)", "trolleyactor")
			assertLocationInTime("4", "3", "W", 10000)
			assertNotMovingInTime(2000)
			
			println("checkTrolleyStop -> forward goto(home)")
			actor!!.forward("goto", "goto(home)", "trolleyactor")
			delay(2000)
			println("checkTrolleyStop -> forward stopTrolley(0)")
			actor!!.forward("stopTrolley", "stopTrolley(0)", "trolleyactor")
			assertNotMovingInTime(2000)
			println("checkTrolleyStop -> forward startTrolley(0)")
			actor!!.forward("startTrolley", "startTrolley(0)", "trolleyactor")
			assertLocationInTime("0", "0", "S", 10000)
			assertNotMovingInTime(2000)
			
		}
	}*/

	private suspend fun observe(actor: String, messages: Array<String>) {
		observer = CoapObserverForTesting(actor)
		for (m in messages) observer!!.addObserver(obsChannel, m)
	}

	private suspend fun assertEvent(event: String, verbose: Boolean = true) {
		var result = obsChannel.receive()
		if (verbose) {
			if (result == event) println("assertEvent -> correct event $result detected")
			else println("assertEvent -> wrong event $result detected instead of $event")
		}
		assertEquals(result, event)
	}

	private suspend fun assertNoEventInTime(millis: Int, verbose: Boolean = true) {
		var counter = 0;
		var result: String? = null;
		do {
			result = obsChannel.poll()
			delay(100)
			counter++
		} while (result == null && counter < millis / 100)
		if (verbose) {
			if (counter >= millis / 100) println("assertNoEventInTime -> no events detected")
			else println("assertNoEventInTime -> event $result detected within ${counter * 100} ms")
		}
		assert(counter >= millis / 100)
	}

	private suspend fun assertLocationInTime(x: String, y: String, d: String, millis: Int, verbose: Boolean = true) {
		var counter = 0;
		while (!(x == directionalPlanner.getX() && y == directionalPlanner.getY() && d == directionalPlanner.getD()) && counter < millis / 100) {
			delay(100)
			counter++
		}
		if (verbose) {
			if (counter < millis / 100) println("assertLocationInTime -> target [$x,$y,$d] reached in ${counter * 100} ms")
			else println("assertLocationInTime -> target [$x,$y,$d] not reached in time")
		}
		assert(counter < millis / 100)
	}

	private suspend fun assertNotMovingInTime(millis: Int, verbose: Boolean = true) {
		var counter = 0;
		val posX = directionalPlanner.getX()
		val posY = directionalPlanner.getY()
		val posD = directionalPlanner.getD()
		while ((directionalPlanner.getX() == posX && directionalPlanner.getY() == posY && directionalPlanner.getD() == posD) && counter < millis / 100) {
			delay(100)
			counter++
		}
		if (verbose) {
			if (counter >= millis / 100) println("assertNotMovingInTime -> no movement detected")
			else println("assertNotMovingInTime -> movement detected within ${counter * 100} ms")
		}
		assert(counter >= millis / 100)
	}

}
