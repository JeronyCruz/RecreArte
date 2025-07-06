package edu.ucne.recrearte.data.remote

import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val recreArteingApi: RecreArteingApi
){}