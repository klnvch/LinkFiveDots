package by.klnvch.link5dots.di

import by.klnvch.link5dots.multiplayer.services.GameServiceBluetooth
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingModule {

    @ContributesAndroidInjector
    abstract fun gameServiceBluetooth(): GameServiceBluetooth
}
