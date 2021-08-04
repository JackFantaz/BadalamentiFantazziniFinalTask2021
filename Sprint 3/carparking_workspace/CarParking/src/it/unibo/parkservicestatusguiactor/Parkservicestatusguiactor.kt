/* Generated by AN DISI Unibo */ 
package it.unibo.parkservicestatusguiactor

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Parkservicestatusguiactor ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "setup"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		 var auto = false  
		return { //this:ActionBasciFsm
				state("setup") { //this:State
					action { //it:State
						updateResourceRep( "manual"  
						)
					}
					 transition( edgeName="goto",targetState="receive", cond=doswitch() )
				}	 
				state("receive") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("temperature(VALUE)"), Term.createTerm("temperature(VALUE)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> The temperature is ${payloadArg(0)}")
						}
						if( checkMsgContent( Term.createTerm("slot(SLOTNUM,STATUS)"), Term.createTerm("slot(STATUS)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> The slot ${payloadArg(0)} is now ${payloadArg(1)}")
						}
						if( checkMsgContent( Term.createTerm("outdoorAlarm(N)"), Term.createTerm("outdoorAlarm(N)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> OUTDOOR alarm!")
						}
						if( checkMsgContent( Term.createTerm("outdoorAlarmRevoked(N)"), Term.createTerm("outdoorAlarmRevoked(N)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> OUTDOOR alarm REVOKED!")
						}
						if( checkMsgContent( Term.createTerm("temperatureAlarm(N)"), Term.createTerm("temperatureAlarm(N)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> Temperature alarm!")
								if(  auto  
								 ){forward("fanStart", "fanStart(0)" ,"fanactor" ) 
								}
						}
						if( checkMsgContent( Term.createTerm("temperatureAlarmRevoked(N)"), Term.createTerm("temperatureAlarmRevoked(N)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> Temperature alarm REVOKED!")
								if(  auto  
								 ){forward("fanStop", "fanStop(0)" ,"fanactor" ) 
								}
						}
					}
					 transition(edgeName="t29",targetState="receive",cond=whenEvent("temperature"))
					transition(edgeName="t30",targetState="receive",cond=whenDispatch("slot"))
					transition(edgeName="t31",targetState="fanControl",cond=whenDispatch("fanStart"))
					transition(edgeName="t32",targetState="fanControl",cond=whenDispatch("fanStop"))
					transition(edgeName="t33",targetState="receive",cond=whenEvent("outdoorAlarm"))
					transition(edgeName="t34",targetState="receive",cond=whenEvent("outdoorAlarmRevoked"))
					transition(edgeName="t35",targetState="receive",cond=whenEvent("temperatureAlarm"))
					transition(edgeName="t36",targetState="receive",cond=whenEvent("temperatureAlarmRevoked"))
					transition(edgeName="t37",targetState="setAuto",cond=whenDispatch("fanAuto"))
					transition(edgeName="t38",targetState="trolleyControl",cond=whenDispatch("startTrolley"))
					transition(edgeName="t39",targetState="trolleyControl",cond=whenDispatch("stopTrolley"))
				}	 
				state("fanControl") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("fanStart(N)"), Term.createTerm("fanStart(0)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> redirecting fanStart(0)")
								forward("fanStart", "fanStart(0)" ,"fanactor" ) 
						}
						if( checkMsgContent( Term.createTerm("fanStop(N)"), Term.createTerm("fanStop(0)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> redirecting fanStop(0)")
								forward("fanStop", "fanStop(0)" ,"fanactor" ) 
						}
					}
					 transition( edgeName="goto",targetState="receive", cond=doswitch() )
				}	 
				state("trolleyControl") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("startTrolley(N)"), Term.createTerm("startTrolley(0)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> redirecting startTrolley(0)")
								forward("startTrolley", "startTrolley(0)" ,"trolleyactor" ) 
						}
						if( checkMsgContent( Term.createTerm("stopTrolley(N)"), Term.createTerm("stopTrolley(0)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Manager's GUI feedback -> redirecting stopTrolley(0)")
								forward("stopTrolley", "stopTrolley(0)" ,"trolleyactor" ) 
						}
					}
					 transition( edgeName="goto",targetState="receive", cond=doswitch() )
				}	 
				state("setAuto") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("fanAuto(STATUS)"), Term.createTerm("fanAuto(STATUS)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								if(  payloadArg(0) == "auto"  
								 ){println("Manager's GUI feedback -> setting fan control to auto")
								 auto = true  
								updateResourceRep( "auto"  
								)
								}
								if(  payloadArg(0) == "manual"  
								 ){println("Manager's GUI feedback -> setting fan control to manual")
								 auto = false  
								updateResourceRep( "manual"  
								)
								}
						}
					}
					 transition( edgeName="goto",targetState="receive", cond=doswitch() )
				}	 
			}
		}
}
