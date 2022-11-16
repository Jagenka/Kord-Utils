package de.jagenka

import de.jagenka.ArgumentType.*

data class Argument(val name: String, val type: ArgumentType)
{
    fun hasFittingType(string: String): Boolean
    {
        return when (type)
        {
            LITERAL -> string == name
            STRING -> true
            INT -> string.toIntOrNull() != null
            DOUBLE -> string.toDoubleOrNull() != null
        }
    }
}
