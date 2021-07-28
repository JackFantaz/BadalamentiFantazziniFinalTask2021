package carparking.webgui

import connQak.ConnectionType
import connQak.connQakBase
import it.unibo.kactor.MsgUtil
import it.unibo.webBasicrobotqak.CoapSupport
import it.unibo.webBasicrobotqak.WebPageCoapHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
class BaseController {
/*
    @Value("\${spring.application.name}")
    var appName: String? = null

    @Autowired
    var simpMessagingTemplate: SimpMessagingTemplate? = null

    //val basicrobotAddress = "127.0.0.1"
    val carparkingAddress = "127.0.0.1"
    lateinit var basicrobotConnection: connQakBase

    var answerChannel = Channel<String>()
    lateinit var coapsupport: CoapSupport
    val coapObserver = WebPageCoapHandler(this, null)

    init {
        connQak.robothostAddr = carparkingAddress
        basicrobotConnection = connQakBase.create(ConnectionType.TCP)
        basicrobotConnection.createConnection()
        coapsupport = CoapSupport("coap://${connQak.robothostAddr}:${connQak.robotPort}", "ctxcarparking/parkserviceguiactor")
        coapsupport.observeResource(coapObserver)
    }

    @GetMapping("/")
    fun homePage(model: Model): String {
        println("/ $model")
        // model.addAttribute("arg", appName)
        model.addAttribute("arg1", "<none_out>")
        model.addAttribute("arg2", "<none_in>")
        return "welcome"
    }

    /*@GetMapping("/robotmove")
    fun doMove(
        viewmodel: Model,
        @RequestParam(name = "move", required = false, defaultValue = "h") robotmove: String
    ): String {
        println("/robotmove $viewmodel $robotmove")
        val message = when (robotmove) {
            "w" -> MsgUtil.buildRequest("carparkinggui", "step", "step(350)", connQak.qakdestination)
            "l" -> MsgUtil.buildDispatch("carparkinggui", "cmd", "cmd(l)", connQak.qakdestination)
            "r" -> MsgUtil.buildDispatch("carparkinggui", "cmd", "cmd(r)", connQak.qakdestination)
            else -> MsgUtil.buildDispatch("carparkinggui", "cmd", "cmd(h)", connQak.qakdestination)
        }
        basicrobotConnection.forward(message)
        viewmodel.addAttribute("arg", message.msgContent())
        return "welcome"
    }*/

    @GetMapping("/carparking")
    fun carparking(
        viewmodel: Model,
        @RequestParam(name = "move", required = false, defaultValue = "") button: String
    ): String {
        println("/robotmove $viewmodel $button")
        val message = when (button) {
            "enter" -> MsgUtil.buildDispatch("clientsgui", "enterRequest", "enterRequest(0)", connQak.qakdestination)
            "car" -> MsgUtil.buildDispatch("clientsgui", "carEnter", "carEnter(0)", connQak.qakdestination)
            "exit" -> MsgUtil.buildDispatch("clientsgui", "exitRequest", "exitRequest(1)", connQak.qakdestination)
            else -> null
        }
        if (message != null) {


            var answer = "<none_in>"


            coapObserver.channel = answerChannel
            basicrobotConnection.forward(message)
            runBlocking {
                answer = answerChannel.receive()
                //NO //answer = coapsupport.readResource()
                coapObserver.channel = null
            }
            println("~~~ $answer")
            viewmodel.addAttribute("arg1", message.msgContent())
            viewmodel.addAttribute("arg2", answer)

        } else {
            viewmodel.addAttribute("arg1", "<none_out>")
            viewmodel.addAttribute("arg2", "<none_in>")
        }
        return "welcome"
    }

    @ExceptionHandler
    fun handle(ex: Exception): ResponseEntity<*> {
        val responseHeaders = HttpHeaders()
        return ResponseEntity(
            "BaseController ERROR ${ex.message}",
            responseHeaders, HttpStatus.CREATED
        )
    }
*/
}
