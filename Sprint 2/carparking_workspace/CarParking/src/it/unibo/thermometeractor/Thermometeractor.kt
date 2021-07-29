/* Generated by AN DISI Unibo */ 
package it.unibo.thermometeractor

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Thermometeractor ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "polling"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		
				val Mock = carparking.temperature.MockThermometer()
				var Previous = Mock.getTemperature()
				var Temperature = Previous
		return { //this:ActionBasciFsm
				state("polling") { //this:State
					action { //it:State
						 Temperature = Mock.getTemperature()  
						if(  Temperature != Previous  
						 ){emit("temperature", "temperature($Temperature)" ) 
						}
						 Previous = Temperature  
						delay(500) 
					}
					 transition( edgeName="goto",targetState="polling", cond=doswitch() )
				}	 
			}
		}
}
