package com.pokemonCompagnon.wearappandroid

data class Pokemon(
    private var name: String
) {
    private val maxHappiness = 20
    private val maxFood = 20
    private val maxEnergy = 20
    private var happiness = maxHappiness / 2
    private var food = maxFood / 2
    private var energy = maxEnergy / 2

    fun getName(): String {
        return name
    }

    fun setName(str: String) {
        name = str
    }

    fun getHappiness(): Int {
        return happiness
    }

    fun setHappiness(nbr: Int) {
        happiness = nbr
        if (happiness > maxHappiness)
            happiness = maxHappiness
        if (happiness < 0)
            happiness = 0
    }

    fun getFood(): Int {
        return food
    }

    fun setFood(nbr: Int) {
        food = nbr
        if (food > maxFood)
            food = maxFood
        if (food < 0)
            food = 0
    }

    fun getEnergy(): Int {
        return energy
    }

    fun setEnergy(nbr: Int) {
        energy = nbr
        if (energy > maxEnergy)
            energy = maxEnergy
        if (energy < 0)
            energy = 0
    }
}