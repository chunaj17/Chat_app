package com.example.coolchat.ui.user


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coolchat.repository.UserRepository
import com.example.coolchat.room.user.UserCacheEntity
import com.example.coolchat.room.user.UserDao
import com.example.coolchat.util.DataState
import com.example.coolchat.util.MainStateEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class UserViewModel
@Inject
constructor(
    private val userRepository: UserRepository,
    private val dao: UserDao
) : ViewModel() {

    //    private  val TAG = "UserViewModel"
    private val _dataState: MutableLiveData<DataState<List<UserCacheEntity>>> = MutableLiveData()
    val dataState: LiveData<DataState<List<UserCacheEntity>>>
        get() = _dataState

//    private val handler= CoroutineExceptionHandler{ _,e->
//        Log.e(TAG, ": crash occurred",e  )
//    }

    fun setStateEvent(mainStateEvent: MainStateEvent) {
        viewModelScope.launch {
            when (mainStateEvent) {
                is MainStateEvent.GetUserEvents -> {
                    _dataState.value = DataState.Loading
                    userRepository.getUser().collect {
//                        .catch { e ->
//                    }
                        if (it) {
                            dao.get().collect { users ->
                                _dataState.value = DataState.Success(users)
                            }
                        } else {
                            _dataState.value = DataState.Error(Exception("Unknown error"))
                        }
                    }
                }
                is MainStateEvent.None -> {
                    println("none")
                }
            }
        }
    }
}