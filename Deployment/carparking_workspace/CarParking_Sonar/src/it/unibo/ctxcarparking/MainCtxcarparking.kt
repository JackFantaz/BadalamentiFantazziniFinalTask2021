/* Generated by AN DISI Unibo */ 
package it.unibo.ctxcarparking
import it.unibo.kactor.QakContext
import it.unibo.kactor.sysUtil
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
	QakContext.createContexts(
	        "192.168.1.191", this, "carparking.pl", "sysRules.pl"
	)
}
