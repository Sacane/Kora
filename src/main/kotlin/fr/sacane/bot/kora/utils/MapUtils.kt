package fr.sacane.bot.kora.utils



fun <T, K> MutableMap<T, K>.addNotNull(pair: Pair<T?, K>){

    if(pair.first == null){
        return
    }
    this[pair.first!!] = pair.second
}

fun <T, K> MutableMap<T, K>.addAllNotNull(vararg pair: Pair<T?, K>){
    pair.forEach { addNotNull(it) }
}

fun <T, K> MutableMap<T, K>.addAllNotNull(pairs: Collection<Pair<T?, K>>){
    pairs.forEach { addNotNull(it) }
}