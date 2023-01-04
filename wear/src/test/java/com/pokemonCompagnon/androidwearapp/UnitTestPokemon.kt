package com.pokemonCompagnon.androidwearapp

import com.pokemonCompagnon.wearappandroid.Pokemon
import org.junit.Test

import org.junit.Assert.*

class UnitTestPokemon {
    @Test
    fun initData() {
        val pikachu: Pokemon = Pokemon("pikachu")
        assertEquals(pikachu.getName(), "pikachu")
        assertEquals(pikachu.getEnergy(), 10)
        assertEquals(pikachu.getFood(), 10)
        assertEquals(pikachu.getHappiness(), 10)
    }

    @Test
    fun changeName() {
        val pikachu: Pokemon = Pokemon("pikachu")
        pikachu.setName("pad")
        assertEquals(pikachu.getName(), "pad")
    }

    @Test
    fun updateValues() {
        val pikachu: Pokemon = Pokemon("pikachu")
        pikachu.setFood(1)
        pikachu.setEnergy(1)
        pikachu.setHappiness(1)
        assertEquals(pikachu.getEnergy(), 1)
        assertEquals(pikachu.getFood(), 1)
        assertEquals(pikachu.getHappiness(), 1)
        pikachu.setFood(200)
        pikachu.setEnergy(200)
        pikachu.setHappiness(200)
        assertEquals(pikachu.getEnergy(), 20)
        assertEquals(pikachu.getFood(), 20)
        assertEquals(pikachu.getHappiness(), 20)
        pikachu.setFood(-200)
        pikachu.setEnergy(-200)
        pikachu.setHappiness(-200)
        assertEquals(pikachu.getEnergy(), 0)
        assertEquals(pikachu.getFood(), 0)
        assertEquals(pikachu.getHappiness(), 0)
    }
}
