package by.klnvch.link5dots.di

import by.klnvch.link5dots.multiplayer.services.GameServiceBluetooth
import by.klnvch.link5dots.multiplayer.services.GameServiceNsd
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBindingModule {
    @ContributesAndroidInjector
    abstract fun gameServiceNsd(): GameServiceNsd

    @ContributesAndroidInjector
    abstract fun gameServiceBluetooth(): GameServiceBluetooth
}
