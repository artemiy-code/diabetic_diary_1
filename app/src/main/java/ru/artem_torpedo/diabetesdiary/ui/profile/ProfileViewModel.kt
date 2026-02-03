package ru.artem_torpedo.diabetesdiary.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.artem_torpedo.diabetesdiary.data.local.entity.ProfileEntity
import ru.artem_torpedo.diabetesdiary.data.repositoriy.ProfileRepository

class ProfileViewModel(application: Application) :
    AndroidViewModel(application) {

    private val repository = ProfileRepository(application)

    val profiles = MutableLiveData<List<ProfileEntity>>()

    fun loadProfiles() {
        viewModelScope.launch {
            profiles.postValue(repository.getProfiles())
        }
    }

    fun addProfile(name: String) {
        viewModelScope.launch {
            repository.addProfile(
                ProfileEntity(name = name)
            )
            loadProfiles()
        }
    }
}