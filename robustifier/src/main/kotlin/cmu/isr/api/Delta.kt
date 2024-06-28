package cmu.isr.api

import cmu.isr.tolerance.delta.DeltaDFS
import cmu.isr.ts.DetLTS
import cmu.isr.ts.LTS
import cmu.isr.ts.alphabet
import cmu.isr.ts.lts.CompactDetLTS
import cmu.isr.ts.lts.CompactLTS
import cmu.isr.ts.lts.ltsa.write
import cmu.isr.ts.parallel
import errorTrace
import satisfies

/**
 * Calculate the robustness \Delta of a decomposed software system.
 *
 * In the future, this may be amended to support environmental properties.
 *
 * @param env Environmental LTS
 * @param ctrl Controller / system LTS
 * @param prop Controller safety property LTS
 * @return The \Delta set of all maximal transition sets, representing the system's robustness
 *
 * @author Ian Dardik, Will Morris
 */
fun calculateDelta(env : LTS<*, String>,
                   ctrl : LTS<*, String>,
                   prop : DetLTS<*, String>)
                   : Set<Set<Triple<Int, String, Int>>>? {

    // External interface allows for any LTS.
    // Internally, we use compact LTS
    val env = env as CompactLTS<String>
    val ctrl = ctrl as CompactLTS<String>
    val prop = prop as CompactDetLTS<String>

    return when(satisfies(parallel(env,ctrl), prop)) {
        // Using faster DeltaDFS function. CLI version has a variety of different fns for testing.
        true -> DeltaDFS(env, ctrl, prop, false).compute()

        false -> {
            val trace = errorTrace(parallel(env, ctrl), prop)
            println("Error: ~(E||C |= P)")
            println("Trace: $trace")

            val aut = parallel(env, ctrl)
            write(System.out, aut, aut.alphabet())

            null
        }
    }
}