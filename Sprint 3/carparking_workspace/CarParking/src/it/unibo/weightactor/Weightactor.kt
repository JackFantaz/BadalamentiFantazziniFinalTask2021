/* Generated by AN DISI Unibo */ 
package it.unibo.weightactor

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Weightactor ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "setup"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				val mock = carparking.presence.PresenceWeight(carparking.presence.MockWeightSensor())
				var previous = false
				var present = false
		return { //this:ActionBasciFsm
				state("setup") { //this:State
					action { //it:State
						emit("indoorCleared", "indoorCleared(0)" ) 
					}
					 transition( edgeName="goto",targetState="polling", cond=doswitch() )
				}	 
				state("polling") { //this:State
					action { //it:State
						 present = mock.isPresent()  
						if(  present && previous!=present  
						 ){emit("indoorOccupied", "indoorOccupied(0)" ) 
						updateResourceRep( "indoorOccupied(0)"  
						)
						}
						if(  !present && previous!=present  
						 ){emit("indoorCleared", "indoorCleared(0)" ) 
						updateResourceRep( "indoorCleared(0)"  
						)
						}
						 previous = present  
						stateTimer = TimerActor("timer_polling", 
							scope, context!!, "local_tout_weightactor_polling", 500.toLong() )
					}
					 transition(edgeName="t34",targetState="polling",cond=whenTimeout("local_tout_weightactor_polling"))   
					transition(edgeName="t35",targetState="response",cond=whenRequest("lastEvent"))
				}	 
				state("response") { //this:State
					action { //it:State
						if(  present  
						 ){answer("lastEvent", "indoorOccupied", "indoorOccupied(0)"   )  
						}
						else
						 {answer("lastEvent", "indoorCleared", "indoorCleared(0)"   )  
						 }
					}
					 transition( edgeName="goto",targetState="polling", cond=doswitch() )
				}	 
			}
		}
}
