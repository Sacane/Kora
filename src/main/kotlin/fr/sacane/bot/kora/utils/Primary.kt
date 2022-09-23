package fr.sacane.bot.kora.utils

import kotlin.time.DurationUnit


fun MutableMap<Long, DurationUnit>.addNotNull(pair: Pair<Long?, DurationUnit>){
    if(pair.first == null) return
    this[pair.first!!] = pair.second
}

fun MutableMap<Long, DurationUnit>.addAllNotNull(vararg pair: Pair<Long?, DurationUnit>){
    pair.forEach { addNotNull(it) }
}

fun MutableMap<Long, DurationUnit>.addAllNotNull(pairs: Collection<Pair<Long?, DurationUnit>>){
    pairs.forEach { addNotNull(it) }
}