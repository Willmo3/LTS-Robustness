package cmu.isr.ts.lts.ltsa

import cmu.isr.ts.alphabet
import cmu.isr.ts.nfa.determinise
import net.automatalib.automata.fsa.DFA
import net.automatalib.automata.fsa.NFA
import net.automatalib.commons.util.Holder
import net.automatalib.util.ts.traversal.TSTraversal
import net.automatalib.util.ts.traversal.TSTraversalAction
import net.automatalib.util.ts.traversal.TSTraversalVisitor
import net.automatalib.words.Alphabet
import java.io.OutputStream

object FSPWriter {
  fun <S, I> write(output: OutputStream, dfa: DFA<S, I>, inputs: Alphabet<I>) {
    val builder = StringBuilder()
    val writer = output.writer()
    TSTraversal.breadthFirst(dfa, inputs, FSPWriterVisitor(builder, dfa, inputs))
    if (builder.endsWith(" | ")) {
      builder.setLength(builder.length - 3)
      builder.appendLine(").")
    }
    writer.write(builder.toString())
    writer.flush()
  }

  fun <S, I> write(output: OutputStream, dfa: DFA<S, I>) {
    val inputs = dfa.alphabet()
    val builder = StringBuilder()
    val writer = output.writer()
    TSTraversal.breadthFirst(dfa, inputs, FSPWriterVisitor(builder, dfa, inputs))
    if (builder.endsWith(" | ")) {
      builder.setLength(builder.length - 3)
      builder.appendLine(").")
    }
    writer.write(builder.toString())
    writer.flush()
  }

  fun <S, I> write(output: OutputStream, nfa: NFA<S, I>) {
    write(output, determinise(nfa))
  }

  // thanks https://www.baeldung.com/kotlin/check-if-string-is-numeric
  fun isNumeric(toCheck: String): Boolean {
    return toCheck.all { char -> char.isDigit() }
  }

  fun transformIndices(actionWithDot: String): String {
    var actionBuilder = StringBuilder()
    val indices = actionWithDot.split('.')
    for (i in indices.indices) {
      val token = indices[i]
      when (i) {
        0 -> actionBuilder.append(token)
        else ->
          if (isNumeric(token)) {
            actionBuilder.append("[").append(token).append("]")
          } else {
            actionBuilder.append(".").append(token)
          }
      }
    }
    return actionBuilder.toString()
  }

  private class FSPWriterVisitor<S, I>(
    val builder: StringBuilder,
    val dfa: DFA<S, I>,
    val inputs: Alphabet<I>
  ) : TSTraversalVisitor<S, I, S, Void?> {
    private val visited = mutableSetOf<S>()

    override fun processInitial(state: S, outData: Holder<Void?>): TSTraversalAction {
      return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: S, data: Void?): Boolean {
      return if (state !in visited) {
        visited.add(state)
        if (builder.endsWith(" | ")) {
          builder.setLength(builder.length - 3)
          builder.appendLine("),")
        }
        builder.append("S$state = (")
        true
      } else {
        false
      }
    }

    override fun processTransition(
      source: S,
      srcData: Void?,
      input: I,
      transition: S,
      succ: S,
      outData: Holder<Void?>?
    ): TSTraversalAction {
      // check deadlock state
      var isDeadlock = true
      for (a in inputs) {
        if (dfa.getTransition(succ, a) != null) {
          isDeadlock = false
          break
        }
      }
      val action = transformIndices(input.toString())
      return if (isDeadlock) {
        builder.append("$action -> STOP | ")
        TSTraversalAction.IGNORE
      } else {
        builder.append("$action -> S$succ | ")
        TSTraversalAction.EXPLORE
      }
    }

  }

  fun <S, I> write(output: OutputStream, nfa: NFA<S, I>, inputs: Alphabet<I>) {
    val builder = StringBuilder()
    val writer = output.writer()
    TSTraversal.breadthFirst(nfa, inputs, NFAFSPWriterVisitor(builder, nfa, inputs))
    if (builder.endsWith(" | ")) {
      builder.setLength(builder.length - 3)
      builder.appendLine(").")
    } else if (builder.endsWith("(")) {
      builder.setLength(builder.length - 1)
      builder.appendLine("STOP.")
    }
    writer.write(builder.toString())
    writer.flush()
  }

  private class NFAFSPWriterVisitor<S, I>(
    val builder: StringBuilder,
    val dfa: NFA<S, I>,
    val inputs: Alphabet<I>
  ) : TSTraversalVisitor<S, I, S, Void?> {
    private val visited = mutableSetOf<S>()

    override fun processInitial(state: S, outData: Holder<Void?>): TSTraversalAction {
      return TSTraversalAction.EXPLORE
    }

    override fun startExploration(state: S, data: Void?): Boolean {
      return if (state !in visited) {
        visited.add(state)
        if (builder.endsWith(" | ")) {
          builder.setLength(builder.length - 3)
          builder.appendLine("),")
        } else if (builder.endsWith("(")) {
          builder.setLength(builder.length - 1)
          builder.appendLine("STOP,")
        }
        builder.append("S$state = (")
        true
      } else {
        false
      }
    }

    override fun processTransition(
      source: S,
      srcData: Void?,
      input: I,
      transition: S,
      succ: S,
      outData: Holder<Void?>?
    ): TSTraversalAction {
      // check deadlock state
      var isDeadlock = true
      for (a in inputs) {
        if (dfa.getTransitions(succ, a) != null) {
          isDeadlock = false
          break
        }
      }
      val action = transformIndices(input.toString())
      return if (isDeadlock) {
        builder.append("$action -> STOP | ")
        TSTraversalAction.IGNORE
      } else {
        builder.append("$action -> S$succ | ")
        TSTraversalAction.EXPLORE
      }
    }

  }
}
