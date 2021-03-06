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
import org.eclipse.californium.core.CoapClient

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class Sprint3Test {

	companion object {

		var actor: ActorBasic? = null
		var syncChannel = Channel<String>()
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
			runBlocking { delay(2000) }
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
		runBlocking { delay(2000) }
	}
	
	@Test
	fun showCase() {
		runBlocking {
		
			val parkserviceObs = CarparkingCoapObserver("parkserviceguiactor", blocking=false)
			val temperaturesentinelObs = CarparkingCoapObserver("temperaturesentinelactor", blocking=false)
			val fanObs = CarparkingCoapObserver("fanactor", blocking=false)
			
			// TWO CARS IN
			actor!!.forward("enterRequest", "enterRequest(0)", "parkserviceguiactor")
			actor!!.forward("carEnter", "carEnter(0)", "parkserviceguiactor")
			assertLocationInTime("6", "0", "N", 60000)
			assertLocationInTime("4", "3", "W", 60000)
			val tokenid1 = parkserviceObs.observePayload()
			actor!!.forward("enterRequest", "enterRequest(0)", "parkserviceguiactor")
			actor!!.forward("carEnter", "carEnter(0)", "parkserviceguiactor")
			assertLocationInTime("6", "0", "N", 60000)
			assertLocationInTime("4", "2", "W", 60000)
			val tokenid2 = parkserviceObs.observePayload()
			
			// AUTO FAN CHECK
			actor!!.forward("fanAuto", "fanAuto(auto)", "parkservicestatusguiactor")
			delay(3000)
			actor!!.emit("temperature", "temperature(60.0)")
			assertEvent(temperaturesentinelObs, "temperatureAlarm(0)")
			assertEvent(fanObs, "fanStart(0)")
			delay(3000)
			actor!!.emit("temperature", "temperature(20.0)")
			assertEvent(temperaturesentinelObs, "temperatureAlarmRevoked(0)")
			assertEvent(fanObs, "fanStop(0)")
			delay(3000)
			actor!!.forward("fanAuto", "fanAuto(manual)", "parkservicestatusguiactor")
			
			// EXIT WITH TROLLEY STOP-START
			actor!!.forward("exitRequest", "exitRequest($tokenid1)", "parkserviceguiactor")
			delay(3000)
			actor!!.forward("stopTrolley", "stopTrolley(0)", "parkservicestatusguiactor")
			assertNotMovingInTime(3000)
			actor!!.forward("startTrolley", "startTrolley(0)", "parkservicestatusguiactor")
			assertLocationInTime("4", "3", "W", 60000)
			assertLocationInTime("6", "4", "S", 60000)
			assertLocationInTime("0", "0", "S", 60000)
			
			// EXPIRED TOKENID
			actor!!.forward("exitRequest", "exitRequest($tokenid1)", "parkserviceguiactor")
			assertNotice(parkserviceObs, "tokenid(invalid)")
			delay(3000)
			
			// EXIT REQUEST WITH OUTDOOR-AREA OCCUPIED
			actor!!.emit("outdoorOccupied", "outdoorOccupied(0)")
			actor!!.forward("exitRequest", "exitRequest($tokenid2)", "parkserviceguiactor")
			assertNotice(parkserviceObs, "outdoorArea(occupied)")
			assertNotMovingInTime(3000)

			// EXIT WITH OUTDOOR-AREA CLEARED
			actor!!.emit("outdoorCleared", "outdoorCleared(0)")
			actor!!.forward("exitRequest", "exitRequest($tokenid2)", "parkserviceguiactor")
			assertNotice(parkserviceObs, "exitRequest(received)")
			assertLocationInTime("4", "2", "W", 60000)
			assertLocationInTime("6", "4", "S", 60000)
			assertLocationInTime("0", "0", "S", 60000)

		}
	}
	
	/*@Test
	fun showCaseAlternative() {
		runBlocking {
		
			val parkserviceObs = CarparkingCoapObserver("parkserviceguiactor")
			val temperaturesentinelObs = CarparkingCoapObserver("temperaturesentinelactor")
			val fanObs = CarparkingCoapObserver("fanactor")
			
			// TWO CARS IN
			actor!!.forward("enterRequest", "enterRequest(0)", "parkserviceguiactor")
			assertSlotnum(parkserviceObs, "6")
			actor!!.forward("carEnter", "carEnter(0)", "parkserviceguiactor")
			val tokenid1 = parkserviceObs.observePayload()
			assertLocationInTime("6", "0", "N", 60000)
			assertLocationInTime("4", "3", "W", 60000)
			actor!!.forward("enterRequest", "enterRequest(0)", "parkserviceguiactor")
			assertSlotnum(parkserviceObs, "5")
			actor!!.forward("carEnter", "carEnter(0)", "parkserviceguiactor")
			val tokenid2 = parkserviceObs.observePayload()
			assertLocationInTime("6", "0", "N", 60000)
			assertLocationInTime("4", "2", "W", 60000)
			
			// AUTO FAN CHECK
			actor!!.forward("fanAuto", "fanAuto(auto)", "parkservicestatusguiactor")
			delay(3000)
			actor!!.emit("temperature", "temperature(60.0)")
			assertEvent(temperaturesentinelObs, "temperatureAlarm(0)")
			assertEvent(fanObs, "fanStart(0)")
			delay(3000)
			actor!!.emit("temperature", "temperature(20.0)")
			assertEvent(temperaturesentinelObs, "temperatureAlarmRevoked(0)")
			assertEvent(fanObs, "fanStop(0)")
			delay(3000)
			actor!!.forward("fanAuto", "fanAuto(manual)", "parkservicestatusguiactor")
			
			// EXIT WITH TROLLEY STOP-START
			actor!!.forward("exitRequest", "exitRequest($tokenid1)", "parkserviceguiactor")
			assertNotice(parkserviceObs, "exitRequest(received)")
			delay(3000)
			actor!!.forward("stopTrolley", "stopTrolley(0)", "parkservicestatusguiactor")
			assertNotMovingInTime(3000)
			actor!!.forward("startTrolley", "startTrolley(0)", "parkservicestatusguiactor")
			assertLocationInTime("4", "3", "W", 60000)
			assertLocationInTime("6", "4", "S", 60000)
			assertLocationInTime("0", "0", "S", 60000)
			
			// EXPIRED TOKENID
			actor!!.forward("exitRequest", "exitRequest($tokenid1)", "parkserviceguiactor")
			assertNotice(parkserviceObs, "tokenid(invalid)")
			delay(3000)
			
			// EXIT REQUEST WITH OUTDOOR-AREA OCCUPIED
			actor!!.emit("outdoorOccupied", "outdoorOccupied(0)")
			actor!!.forward("exitRequest", "exitRequest($tokenid2)", "parkserviceguiactor")
			assertNotice(parkserviceObs, "outdoorArea(occupied)")
			assertNotMovingInTime(3000)

			// EXIT WITH OUTDOOR-AREA CLEARED
			actor!!.emit("outdoorCleared", "outdoorCleared(0)")
			actor!!.forward("exitRequest", "exitRequest($tokenid2)", "parkserviceguiactor")
			assertNotice(parkserviceObs, "exitRequest(received)")
			assertLocationInTime("4", "2", "W", 60000)
			assertLocationInTime("6", "4", "S", 60000)
			assertLocationInTime("0", "0", "S", 60000)

		}
	}*/

	/*@Test
	fun checkCleanSequence() {
		runBlocking {

			val cco = CarparkingCoapObserver("parkserviceguiactor", blocking = true, verbose = false)

			println("checkCleanSequence -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			val slotnum = cco.observePayload()
			println("checkCleanSequence -> received SLOTNUM $slotnum")
			assertNotMovingInTime(3000)

			println("checkCleanSequence -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			val tokenid = cco.observePayload()
			println("checkCleanSequence -> received TOKENID $tokenid")
			assertLocationInTime("6", "0", "N", 10000)
			assertLocationInTime("4", "3", "W", 10000)
			assertLocationInTime("0", "0", "S", 10000)
			assertNotMovingInTime(3000)

			println("checkCleanSequence -> forward exitRequest($tokenid)")
			actor!!.forward("exitRequest", "exitRequest($tokenid)", "parkmanagerserviceactor")
			val notice = cco.observePayload()
			println("checkCleanSequence -> received NOTICE $notice")
			assertLocationInTime("4", "3", "W", 10000)
			assertLocationInTime("6", "4", "S", 10000)
			assertLocationInTime("0", "0", "S", 50000)
			assertNotMovingInTime(3000)

		}
	}*/

	/*@Test
	fun checkRobustSequence() {
		runBlocking {

			var cco = CarparkingCoapObserver("parkserviceguiactor", blocking = false, verbose = true)
			var tokenid = "12345"

			assertNotMovingInTime(3000)

			for (i in 1..2) {

				println("checkRobustSequence -> forward exitRequest($tokenid)")
				actor!!.forward("exitRequest", "exitRequest($tokenid)", "parkmanagerserviceactor")
				assertNotMovingInTime(3000)

				println("checkRobustSequence -> forward enterRequest(0)")
				actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
				assertNotMovingInTime(3000)

				println("checkRobustSequence -> forward carEnter(0)")
				actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
				assertLocationInTime("6", "0", "N", 10000)
				assertLocationInTime("4", "3", "W", 10000)
				tokenid = cco.observePayload()
				assertLocationInTime("0", "0", "S", 10000)
				assertNotMovingInTime(3000)

				println("checkRobustSequence -> forward exitRequest($tokenid)")
				actor!!.forward("exitRequest", "exitRequest($tokenid)", "parkmanagerserviceactor")
				assertLocationInTime("4", "3", "W", 10000)
				assertLocationInTime("6", "4", "S", 10000)
				assertLocationInTime("0", "0", "S", 50000)
				assertNotMovingInTime(3000)

			}

		}
	}*/

	/*@Test
	fun checkDoors() {
		runBlocking {

			var cco = CarparkingCoapObserver("parkserviceguiactor", blocking = false, verbose = true)

			println("checkDoors -> emit indoorOccupied(0)")
			actor!!.emit("indoorOccupied", "indoorOccupied(0)")

			println("checkDoors -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			println("checkDoors -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			assertNotMovingInTime(3000)

			println("checkDoors -> emit indoorCleared(0)")
			actor!!.emit("indoorCleared", "indoorCleared(0)")

			println("checkDoors -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			assertLocationInTime("6", "0", "N", 10000)
			assertLocationInTime("0", "0", "S", 20000)
			var tokenid = cco.observePayload()

			println("checkDoors -> emit outdoorOccupied(0)")
			actor!!.emit("outdoorOccupied", "outdoorOccupied(0)")

			println("checkDoors -> forward exitRequest($tokenid)")
			actor!!.forward("exitRequest", "exitRequest($tokenid)", "parkmanagerserviceactor")
			assertNotMovingInTime(3000)

			println("checkDoors -> emit outdoorCleared(0)")
			actor!!.emit("outdoorCleared", "outdoorCleared(0)")

			println("checkDoors -> forward exitRequest($tokenid)")
			actor!!.forward("exitRequest", "exitRequest($tokenid)", "parkmanagerserviceactor")
			assertLocationInTime("6", "4", "S", 20000)
			assertLocationInTime("0", "0", "S", 50000)

		}
	}*/

	/*@Test
	fun checkSensorsAndActuators() {
		runBlocking {

			val ccow = CarparkingCoapObserver("weightactor", blocking = true, verbose = false)
			println("checkSensorsAndActuators -> please raise weight above threshold (default 60)")
			assertEvent(ccow, "indoorOccupied(0)")
			println("checkSensorsAndActuators -> please lower weight below threshold (default 60)")
			assertEvent(ccow, "indoorCleared(0)")

			var ccos = CarparkingCoapObserver("sonaractor", blocking = true, verbose = false)
			println("checkSensorsAndActuators -> please bring sonar below threshold (default 40)")
			assertEvent(ccos, "outdoorOccupied(0)")
			println("checkSensorsAndActuators -> please move sonar above threshold (default 40)")
			assertEvent(ccos, "outdoorCleared(0)")

			var ccot = CarparkingCoapObserver("thermometeractor", blocking = false, verbose = false)
			println("checkSensorsAndActuators -> please set temperature to 10.0 degrees and press ENTER on console")
			readLine()
			assertEvent(ccot, "temperature(10.0)")

			actor!!.forward("fanStart", "fanStart(0)", "fanactor")
			println("checkSensorsAndActuators -> please wait for fan to turn on and press ENTER on console")
			readLine()
			actor!!.forward("fanStop", "fanStop(0)", "fanactor")
			println("checkSensorsAndActuators -> please wait for fan to turn off and press ENTER on console")
			readLine()

		}
	}*/

	/*@Test
	fun checkAlarms() {
		runBlocking {

			val ccot = CarparkingCoapObserver("temperaturesentinelactor", blocking = true, verbose = false)
			println("checkAlarms -> emit temperature(15.0)")
			actor!!.emit("temperature", "temperature(15.0)")
			assertNoEventInTime(ccot, 1000)
			println("checkAlarms -> emit temperature(45.0)")
			actor!!.emit("temperature", "temperature(45.0)")
			assertEvent(ccot, "temperatureAlarm(0)")
			println("checkAlarms -> emit temperature(15.0)")
			actor!!.emit("temperature", "temperature(15.0)")
			assertEvent(ccot, "temperatureAlarmRevoked(0)")

			val ccoo = CarparkingCoapObserver("outdoorsentinelactor", blocking = true, verbose = false)
			println("checkAlarms -> emit outdoorCleared(0)")
			actor!!.emit("outdoorCleared", "outdoorCleared(0)")
			assertNoEventInTime(ccoo, 1000)
			println("checkAlarms -> emit outdoorOccupied(0)")
			actor!!.emit("outdoorOccupied", "outdoorOccupied(0)")
			assertNoEventInTime(ccoo, 4000)
			assertEvent(ccoo, "outdoorAlarm(0)")
			println("checkAlarms -> emit outdoorCleared(0)")
			actor!!.emit("outdoorCleared", "outdoorCleared(0)")
			assertEvent(ccoo, "outdoorAlarmRevoked(0)")

			val ccof = CarparkingCoapObserver("fanactor", blocking = true, verbose = false)
			println("checkAlarms -> forward fanAuto(auto)")
			actor!!.forward("fanAuto", "fanAuto(auto)", "parkservicestatusguiactor")
			println("checkAlarms -> emit temperatureAlarm(0)")
			actor!!.emit("temperatureAlarm", "temperatureAlarm(0)")
			assertEvent(ccof, "fanStart(0)")
			delay(1000)
			println("checkAlarms -> emit temperatureAlarmRevoked(0)")
			actor!!.emit("temperatureAlarmRevoked", "temperatureAlarmRevoked(0)")
			assertEvent(ccof, "fanStop(0)")
			delay(1000)
			println("checkAlarms -> forward fanAuto(manual)")
			actor!!.forward("fanAuto", "fanAuto(manual)", "parkservicestatusguiactor")
			println("checkAlarms -> emit temperatureAlarm(0)")
			actor!!.emit("temperatureAlarm", "temperatureAlarm(0)")
			assertNoEventInTime(ccof, 1000)
			println("checkAlarms -> emit temperatureAlarmRevoked(0)")
			actor!!.emit("temperatureAlarmRevoked", "temperatureAlarmRevoked(0)")
			assertNoEventInTime(ccof, 1000)

		}
	}*/

	/*@Test
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

	/*@Test
	fun checkSlots() {
		runBlocking {

			var cco = CarparkingCoapObserver("parkserviceguiactor", blocking = true, verbose = false)

			//SIX CARS + 1
			println("checkSlots -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			var slotnum6 = cco.observePayload()

			println("checkSlots -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			var tokenid6 = cco.observePayload()
			assertLocationInTime("6", "0", "N", 10000) //indoor
			assertLocationInTime("4", "3", "W", 10000) //parking6

			println("checkSlots -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			var slotnum5 = cco.observePayload()

			println("checkSlots -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			var tokenid5 = cco.observePayload()
			assertLocationInTime("6", "0", "N", 10000) //indoor
			assertLocationInTime("4", "2", "W", 10000) //parking5

			println("checkSlots -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			var slotnum4 = cco.observePayload()

			println("checkSlots -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			var tokenid4 = cco.observePayload()
			assertLocationInTime("6", "0", "N", 10000) //indoor
			assertLocationInTime("4", "1", "W", 10000) //parking4

			println("checkSlots -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			var slotnum3 = cco.observePayload()

			println("checkSlots -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			var tokenid3 = cco.observePayload()
			assertLocationInTime("6", "0", "N", 10000) //indoor
			assertLocationInTime("1", "3", "E", 10000) //parking3

			println("checkSlots -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			var slotnum2 = cco.observePayload()

			println("checkSlots -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			var tokenid2 = cco.observePayload()
			assertLocationInTime("6", "0", "N", 10000) //indoor
			assertLocationInTime("1", "2", "E", 10000) //parking2

			println("checkSlots -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			var slotnum1 = cco.observePayload()

			println("checkSlots -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			var tokenid1 = cco.observePayload()
			assertLocationInTime("6", "0", "N", 10000) //indoor
			assertLocationInTime("1", "1", "E", 10000) //parking1
			assertLocationInTime("0", "0", "S", 10000) //home

			println("checkSlots -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			assertSlotnum(cco, "0")

			//EXIT 4

			println("checkSlots -> forward exitRequest($tokenid4)")
			actor!!.forward("exitRequest", "exitRequest($tokenid4)", "parkmanagerserviceactor")
			assertNotice(cco, "exitRequest(received)")
			assertLocationInTime("4", "1", "W", 10000) //parking4
			assertLocationInTime("6", "4", "S", 10000) //oudoor

			//EXIT 2

			println("checkSlots -> forward exitRequest($tokenid2)")
			actor!!.forward("exitRequest", "exitRequest($tokenid2)", "parkmanagerserviceactor")
			assertNotice(cco, "exitRequest(received)")
			assertLocationInTime("1", "2", "E", 10000) //parking2
			assertLocationInTime("6", "4", "S", 10000) //oudoor
			assertLocationInTime("0", "0", "S", 120000) //home

			//WRONG TOKENID

			println("checkSlots -> forward exitRequest($tokenid4)")
			actor!!.forward("exitRequest", "exitRequest($tokenid4)", "parkmanagerserviceactor")
			assertNotice(cco, "tokenid(invalid)")


			//ENTER PARKING4

			println("checkSlots -> forward enterRequest(0)")
			actor!!.forward("enterRequest", "enterRequest(0)", "parkmanagerserviceactor")
			var slotnum42 = cco.observePayload()

			println("checkSlots -> forward carEnter(0)")
			actor!!.forward("carEnter", "carEnter(0)", "parkmanagerserviceactor")
			var tokenid42 = cco.observePayload()
			assertLocationInTime("6", "0", "N", 10000) //indoor
			assertLocationInTime("4", "1", "W", 10000) //parking4

			//EXIT 6

			println("checkSlots -> forward exitRequest($tokenid6)")
			actor!!.forward("exitRequest", "exitRequest($tokenid6)", "parkmanagerserviceactor")
			assertNotice(cco, "exitRequest(received)")

			//EXIT 1

			println("checkSlots -> forward exitRequest($tokenid1)")
			actor!!.forward("exitRequest", "exitRequest($tokenid1)", "parkmanagerserviceactor")
			assertNotice(cco, "exitRequest(received)")

			//EXIT 3

			println("checkSlots -> forward exitRequest($tokenid3)")
			actor!!.forward("exitRequest", "exitRequest($tokenid3)", "parkmanagerserviceactor")
			assertNotice(cco, "exitRequest(received)")

			//EXIT 4

			println("checkSlots -> forward exitRequest($tokenid42)")
			actor!!.forward("exitRequest", "exitRequest($tokenid42)", "parkmanagerserviceactor")
			assertNotice(cco, "exitRequest(received)")

			//EXIT 5

			println("checkSlots -> forward exitRequest($tokenid5)")
			actor!!.forward("exitRequest", "exitRequest($tokenid5)", "parkmanagerserviceactor")
			assertNotice(cco, "exitRequest(received)")
			assertLocationInTime("0", "0", "S", 120000) //home

		}
	}*/

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

	private suspend fun assertEvent(observer: CarparkingCoapObserver, event: String, verbose: Boolean = true) {
		if (!observer.isBlocking()) delay(500)
		var result = observer.observe()
		if (verbose) {
			if (result == event) println("assertEvent -> correct event $result detected")
			else println("assertEvent -> wrong event $result detected instead of $event")
		}
		assertEquals(result, event)
	}

	private suspend fun assertNoEventInTime(observer: CarparkingCoapObserver, millis: Int, verbose: Boolean = true) {
		var counter = 0;
		var result: String? = null;
		do {
			result = observer.pollNewValue()
			delay(100)
			counter++
		} while (result == null && counter < millis / 100)
		if (verbose) {
			if (counter >= millis / 100) println("assertNoEventInTime -> no events detected")
			else println("assertNoEventInTime -> event $result detected within ${counter * 100} ms")
		}
		assert(counter >= millis / 100)
	}

	private suspend fun assertSlotnum(observer: CarparkingCoapObserver, slotnum: String, verbose: Boolean = true) {
		if (!observer.isBlocking()) delay(500)
		var result = observer.observePayload()
		if (verbose) {
			if (result == slotnum) println("assertSlotnum -> correct SLOTNUM $result detected")
			else println("assertSlotnum -> wrong SLOTNUM $slotnum detected instead of $slotnum")
		}
		assertEquals(result, slotnum)
	}

	private suspend fun assertNotice(observer: CarparkingCoapObserver, notice: String, verbose: Boolean = true) {
		if (!observer.isBlocking()) delay(500)
		var result = observer.observePayload()
		if (verbose) {
			if (result == notice) println("assertNotice -> correct notice $result detected")
			else println("assertNotice -> wrong notice $result detected instead of $notice")
		}
		assertEquals(result, notice)
	}

}
