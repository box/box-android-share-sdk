package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.BoxRequest
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.share.sharerepo.ShareRepo
import com.box.androidsdk.share.utils.ShareSDKTransformer
import com.nhaarman.mockitokotlin2.clearInvocations
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class CollaborationsVMTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val mockShareItem: BoxCollaborationItem = mock()
    private val mockShareRepo: ShareRepo = mock()
    private val mockCollaboration: BoxCollaboration = mock()

    private lateinit var collaborationsShareVM: CollaborationsShareVM
    private lateinit var initialsVM: CollaboratorsInitialsVM

    private val newRole = BoxCollaboration.Role.EDITOR

    private val mockDeleteCollaborationsResponse: BoxResponse<BoxVoid> = mock()
    private val mockUpdateOwnerResponse: BoxResponse<BoxVoid> = mock()
    private val mockUpdateCollaborationResponse: BoxResponse<BoxCollaboration> = mock()
    private val mockFetchCollaborationResponse: BoxResponse<BoxIteratorCollaborations> = mock()
    private val mockFetchRoleResponse: BoxResponse<BoxCollaborationItem> = mock()

    private val mockTransformer: ShareSDKTransformer = mock()
    private val mockCollaborationsPresenterData: PresenterData<BoxIteratorCollaborations> = mock()
    private val mockUpdateCollaborationsPresenterData: PresenterData<BoxCollaboration> = mock()
    private val mockUpdateOwnerPresenterData: PresenterData<BoxVoid> = mock()
    private val mockRolePresenterData: PresenterData<BoxCollaborationItem> = mock()
    private var mockDeleteCollaborationPresenterData: PresenterData<BoxRequest<*,*>> = mock() //need to be changed


    @Before
    fun setup() {

        mockShareRepo()
        mockTransformer()
        collaborationsShareVM = CollaborationsShareVM(mockShareRepo, mockShareItem, mockTransformer)
        initialsVM = CollaboratorsInitialsVM(mockShareRepo, mockShareItem, mockTransformer)

        attachObservers()
    }

    private fun mockTransformer() {
        whenever(mockTransformer.getCollaborationsPresenterData(mockFetchCollaborationResponse)).thenReturn(mockCollaborationsPresenterData)
        whenever(mockTransformer.getUpdateCollaborationPresenterData(mockUpdateCollaborationResponse)).thenReturn(mockUpdateCollaborationsPresenterData)
        whenever(mockTransformer.getUpdateOwnerPresenterData(mockUpdateOwnerResponse)).thenReturn(mockUpdateOwnerPresenterData)
        whenever(mockTransformer.getFetchRolesItemPresenterData(mockFetchRoleResponse)).thenReturn(mockRolePresenterData)


        whenever(mockTransformer.getDeleteCollaborationPresenterData(mockDeleteCollaborationsResponse)).thenReturn(mockDeleteCollaborationPresenterData)

        whenever(mockTransformer.getIntialsViewCollabsPresenterData(mockFetchCollaborationResponse)).thenReturn(mockCollaborationsPresenterData)

    }

    private fun mockShareRepo() {
        whenever(mockShareRepo.updateCollaboration).thenReturn(MutableLiveData())
        whenever(mockShareRepo.collaborations).thenReturn(MutableLiveData())
        whenever(mockShareRepo.updateOwner).thenReturn(MutableLiveData())
        whenever(mockShareRepo.deleteCollaboration).thenReturn(MutableLiveData())
        whenever(mockShareRepo.roleItem).thenReturn(MutableLiveData())

        whenever(mockShareRepo.deleteCollaboration(mockCollaboration)).then {
            val data = mockShareRepo.deleteCollaboration as MutableLiveData
            data.postValue(mockDeleteCollaborationsResponse)
        }

        whenever(mockShareRepo.updateOwner(mockCollaboration)).then {
            val data = mockShareRepo.updateOwner as MutableLiveData
            data.postValue(mockUpdateOwnerResponse)
        }

        whenever(mockShareRepo.updateCollaboration(mockCollaboration, newRole)).then {
            val data = mockShareRepo.updateCollaboration as MutableLiveData
            data.postValue(mockUpdateCollaborationResponse)
        }

        whenever(mockShareRepo.fetchCollaborations(mockShareItem)).then {
            val data = mockShareRepo.collaborations as MutableLiveData
            data.postValue(mockFetchCollaborationResponse)
        }

        whenever(mockShareRepo.fetchRolesFromRemote(mockShareItem)).then {
            val data = mockShareRepo.roleItem as MutableLiveData
            data.postValue(mockFetchRoleResponse)
        }
    }

    private fun attachObservers() {
        collaborationsShareVM.updateCollaboration.observeForever(mock())
        collaborationsShareVM.deleteCollaboration.observeForever(mock())
        collaborationsShareVM.updateOwner.observeForever(mock())
        collaborationsShareVM.roleItem.observeForever(mock())
        collaborationsShareVM.collaborations.observeForever(mock())

        initialsVM.collaborations.observeForever(mock())
    }


    @Test
    fun `test get collaboration transforms to presenter data on live data change`() {
        assertNull(collaborationsShareVM.collaborations.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.fetchCollaborations(mockShareItem)

        Assert.assertEquals(mockCollaborationsPresenterData, collaborationsShareVM.collaborations.value)
    }


    @Test
    fun `test update collaboration transforms to presenter data on live data change`() {
        assertNull(collaborationsShareVM.updateCollaboration.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.updateCollaboration(mockCollaboration, newRole)

        Assert.assertEquals(mockUpdateCollaborationsPresenterData, collaborationsShareVM.updateCollaboration.value)
    }


    @Test
    fun `test update owner transforms to presenter data on live data change`() {
        assertNull(collaborationsShareVM.updateOwner.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.updateOwner(mockCollaboration)

        Assert.assertEquals(mockUpdateOwnerPresenterData, collaborationsShareVM.updateOwner.value)
    }


    @Test
    fun `test delete collaboration transforms to presenter data on live data change`() {
        assertNull(collaborationsShareVM.deleteCollaboration.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.deleteCollaboration(mockCollaboration)

        Assert.assertEquals(mockDeleteCollaborationPresenterData, collaborationsShareVM.deleteCollaboration.value)
    }


    @Test
    fun `test fetch role transforms to presenter data on live data change`() {
        assertNull(collaborationsShareVM.roleItem.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.fetchRolesFromRemote(mockShareItem)

        Assert.assertEquals(mockRolePresenterData, collaborationsShareVM.roleItem.value)
    }

    @Test
    fun `test collaboration initial vm fetch collaboration transforms to presenter data on live data change` () {
        assertNull(initialsVM.collaborations.value)

        //trigger a network request which make changes in LiveData
        mockShareRepo.fetchCollaborations(mockShareItem)

        Assert.assertEquals( mockCollaborationsPresenterData, initialsVM.collaborations.value)

    }
}