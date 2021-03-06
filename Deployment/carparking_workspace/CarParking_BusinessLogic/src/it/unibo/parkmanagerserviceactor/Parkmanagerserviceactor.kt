/* Generated by AN DISI Unibo */ 
package it.unibo.parkmanagerserviceactor

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Parkmanagerserviceactor ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "setup"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				var Slotnum = 0
				var Tokenid = "0"
				var vacant = "v"
				var full = "f"
				val ParkingMap = mutableMapOf(
					1 to "-", 
					2 to "-", 
					3 to "-", 
					4 to "-", 
					5 to "-", 
					6 to "-"
				)
				
				fun slotStatus() : String { 
					var slots = ""
					for((key, value) in ParkingMap){
						if(value.equals("-",true)){
							slots = slots.plus(vacant).plus(" ")
						}else{
							slots = slots.plus(full).plus(" ")
						}
					}
					return slots
				}
		return { //this:ActionBasciFsm
				state("setup") { //this:State
					action { //it:State
						forward("slot", "slot(1,vacant)" ,"parkservicestatusguiactor" ) 
						forward("slot", "slot(2,vacant)" ,"parkservicestatusguiactor" ) 
						forward("slot", "slot(3,vacant)" ,"parkservicestatusguiactor" ) 
						forward("slot", "slot(4,vacant)" ,"parkservicestatusguiactor" ) 
						forward("slot", "slot(5,vacant)" ,"parkservicestatusguiactor" ) 
						forward("slot", "slot(6,vacant)" ,"parkservicestatusguiactor" ) 
						updateResourceRep( slotStatus()  
						)
					}
					 transition( edgeName="goto",targetState="moveToHome", cond=doswitch() )
				}	 
				state("moveToHome") { //this:State
					action { //it:State
						forward("goto", "goto(home)" ,"trolleyactor" ) 
					}
					 transition(edgeName="t0",targetState="acceptIN",cond=whenDispatch("enterRequest"))
					transition(edgeName="t1",targetState="acceptOUT",cond=whenDispatch("exitRequest"))
				}	 
				state("acceptIN") { //this:State
					action { //it:State
						request("indoorStatus", "indoorStatus(0)" ,"sensorsbrokeractor" )  
					}
					 transition(edgeName="t2",targetState="do_acceptIn",cond=whenReply("indoorStatus"))
				}	 
				state("do_acceptIn") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("indoorStatus(N)"), Term.createTerm("indoorStatus(STATUS)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								if(  payloadArg(0) == "occupied"  
								 ){forward("notice", "notice(indoorArea(occupied))" ,"parkserviceguiactor" ) 
								}
						}
					}
					 transition( edgeName="goto",targetState="informIN", cond=doswitchGuarded({ payloadArg(0) == "free"  
					}) )
					transition( edgeName="goto",targetState="moveToHome", cond=doswitchGuarded({! ( payloadArg(0) == "free"  
					) }) )
				}	 
				state("informIN") { //this:State
					action { //it:State
						 Slotnum = 0  
						 
									for((key, value) in ParkingMap){
										if(value.equals("-",true)){
											Slotnum = key
										}
									}
						forward("slotnum", "slotnum($Slotnum)" ,"parkserviceguiactor" ) 
					}
					 transition( edgeName="goto",targetState="do_informIN", cond=doswitchGuarded({ Slotnum > 0  
					}) )
					transition( edgeName="goto",targetState="moveToHome", cond=doswitchGuarded({! ( Slotnum > 0  
					) }) )
				}	 
				state("do_informIN") { //this:State
					action { //it:State
					}
					 transition(edgeName="t3",targetState="moveToIn",cond=whenDispatch("carEnter"))
				}	 
				state("moveToIn") { //this:State
					action { //it:State
						forward("goto", "goto(indoor)" ,"trolleyactor" ) 
					}
					 transition(edgeName="t4",targetState="receipt",cond=whenDispatch("movementDone"))
				}	 
				state("receipt") { //this:State
					action { //it:State
						
									var x = (11111..99999).random().toString()
										while(x==ParkingMap[1] || x==ParkingMap[2] || x==ParkingMap[3] || x==ParkingMap[4] || x==ParkingMap[5] || x==ParkingMap[6]){
											x = (11111..99999).random().toString()
										}
										ParkingMap[Slotnum] = x
										Tokenid = ParkingMap[Slotnum]!!
						forward("tokenid", "tokenid($Tokenid)" ,"parkserviceguiactor" ) 
					}
					 transition( edgeName="goto",targetState="moveToSlotIn", cond=doswitch() )
				}	 
				state("moveToSlotIn") { //this:State
					action { //it:State
						forward("slot", "slot($Slotnum,full)" ,"parkservicestatusguiactor" ) 
						updateResourceRep( slotStatus()  
						)
						if(  Slotnum == 1  
						 ){forward("goto", "goto(parking1)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 2  
						 ){forward("goto", "goto(parking2)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 3  
						 ){forward("goto", "goto(parking3)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 4  
						 ){forward("goto", "goto(parking4)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 5  
						 ){forward("goto", "goto(parking5)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 6  
						 ){forward("goto", "goto(parking6)" ,"trolleyactor" ) 
						}
					}
					 transition(edgeName="t5",targetState="moveToHome",cond=whenDispatch("movementDone"))
				}	 
				state("acceptOUT") { //this:State
					action { //it:State
						 Slotnum = 0  
						if( checkMsgContent( Term.createTerm("exitRequest(TOKENID)"), Term.createTerm("exitRequest(TOKENID)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								 Tokenid = payloadArg(0)  
						}
						 
									for((key, value) in ParkingMap){
										if(value.equals(Tokenid,true)){
											Slotnum = key
										}
									} 
						if(  Slotnum > 0  
						 ){request("outdoorStatus", "outdoorStatus(0)" ,"sensorsbrokeractor" )  
						}
						else
						 {forward("notice", "notice(tokenid(invalid))" ,"parkserviceguiactor" ) 
						 }
					}
					 transition( edgeName="goto",targetState="do_acceptOUT", cond=doswitchGuarded({ Slotnum > 0  
					}) )
					transition( edgeName="goto",targetState="moveToHome", cond=doswitchGuarded({! ( Slotnum > 0  
					) }) )
				}	 
				state("do_acceptOUT") { //this:State
					action { //it:State
					}
					 transition(edgeName="t6",targetState="redo_acceptOUT",cond=whenReply("outdoorStatus"))
				}	 
				state("redo_acceptOUT") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("outdoorStatus(N)"), Term.createTerm("outdoorStatus(STATUS)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								if(  payloadArg(0) == "free"  
								 ){forward("notice", "notice(exitRequest(received))" ,"parkserviceguiactor" ) 
								}
								else
								 {forward("notice", "notice(outdoorArea(occupied))" ,"parkserviceguiactor" ) 
								 }
						}
					}
					 transition( edgeName="goto",targetState="moveToSlotOut", cond=doswitchGuarded({ payloadArg(0) == "free"  
					}) )
					transition( edgeName="goto",targetState="moveToHome", cond=doswitchGuarded({! ( payloadArg(0) == "free"  
					) }) )
				}	 
				state("moveToSlotOut") { //this:State
					action { //it:State
						if(  Slotnum == 1  
						 ){forward("goto", "goto(parking1)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 2  
						 ){forward("goto", "goto(parking2)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 3  
						 ){forward("goto", "goto(parking3)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 4  
						 ){forward("goto", "goto(parking4)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 5  
						 ){forward("goto", "goto(parking5)" ,"trolleyactor" ) 
						}
						if(  Slotnum == 6  
						 ){forward("goto", "goto(parking6)" ,"trolleyactor" ) 
						}
					}
					 transition(edgeName="t7",targetState="moveToOut",cond=whenDispatch("movementDone"))
				}	 
				state("moveToOut") { //this:State
					action { //it:State
						 ParkingMap[Slotnum] = "-"  
						forward("slot", "slot($Slotnum,vacant)" ,"parkservicestatusguiactor" ) 
						updateResourceRep( slotStatus()  
						)
						forward("goto", "goto(outdoor)" ,"trolleyactor" ) 
					}
					 transition(edgeName="t8",targetState="moveToHome",cond=whenDispatch("movementDone"))
				}	 
			}
		}
}
