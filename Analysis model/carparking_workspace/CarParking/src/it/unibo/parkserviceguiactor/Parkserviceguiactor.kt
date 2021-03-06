/* Generated by AN DISI Unibo */ 
package it.unibo.parkserviceguiactor

import it.unibo.kactor.*
import alice.tuprolog.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
	
class Parkserviceguiactor ( name: String, scope: CoroutineScope  ) : ActorBasicFsm( name, scope ){

	override fun getInitialState() : String{
		return "receive"
	}
	@kotlinx.coroutines.ObsoleteCoroutinesApi
	@kotlinx.coroutines.ExperimentalCoroutinesApi			
	override fun getBody() : (ActorBasicFsm.() -> Unit){
		return { //this:ActionBasciFsm
				state("receive") { //this:State
					action { //it:State
						if( checkMsgContent( Term.createTerm("slotnum(SLOTNUM)"), Term.createTerm("slotnum(SLOTNUM)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Client's GUI -> The SLOTNUM is: ${payloadArg(0)}")
						}
						if( checkMsgContent( Term.createTerm("tokenid(TOKENID)"), Term.createTerm("tokenid(TOKENID)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Client's GUI -> The TOKENID is: ${payloadArg(0)}")
						}
						if( checkMsgContent( Term.createTerm("notice(NOTICE)"), Term.createTerm("notice(MESSAGE)"), 
						                        currentMsg.msgContent()) ) { //set msgArgList
								println("Client's GUI -> Notice received: ${payloadArg(0)}")
						}
					}
					 transition(edgeName="t9",targetState="receive",cond=whenDispatch("slotnum"))
					transition(edgeName="t10",targetState="receive",cond=whenDispatch("tokenid"))
					transition(edgeName="t11",targetState="receive",cond=whenDispatch("notice"))
				}	 
			}
		}
}
