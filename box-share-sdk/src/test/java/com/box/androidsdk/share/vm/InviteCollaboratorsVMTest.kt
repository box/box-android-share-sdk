package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.BoxRequestsShare
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.R
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.sharerepo.ShareRepo
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createBoxResponseBatch
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createFailureResponse
import com.box.androidsdk.share.utils.ResponsesCreator.Companion.createSuccessResponse
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import javax.net.ssl.HttpsURLConnection

class InviteCollaboratorsVMTest {
    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private val mockEmailList: Array<String> = arrayOf("boxuser@box.com", "boxuser2@box.com")
    private val mockSelectedRole: BoxCollaboration.Role = BoxCollaboration.Role.EDITOR
    private val mockFilter: String = "filter"
    private val mockShareItem: BoxCollaborationItem = mock()


    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockAddCollabsResponse: BoxResponse<BoxResponseBatch> = mock()

    private val mockFailedToAddException: BoxException = mock()
    private val mockHttpForbiddenException: BoxException = mock()
    private val mockBoxNetworkErrorException: BoxException = mock()
    private val mockAlreadyCollabException: BoxException = mock()
    private val mockBadRequestException: BoxException = mock()

    private val mockGetInviteesResult: BoxIteratorInvitees = mock()
    private val mockFetchRoleItemResult: BoxCollaborationItem = mock()
    private lateinit var mockAddCollabResult: BoxResponseBatch

    private val mockShareRepo: ShareRepo = mock()

    private lateinit var inviteCollabVM: InviteCollaboratorsShareVM

    @Before
    fun setup() {
        mockShareRepo()

        inviteCollabVM = InviteCollaboratorsShareVM(mockShareRepo, mockShareItem)

        attachObservers()
        createExceptions()
    }

    private fun mockShareRepo() {
        whenever(mockShareRepo.fetchRoleItem).thenReturn(MutableLiveData())
        whenever(mockShareRepo.inviteCollabsBatch).thenReturn(MutableLiveData())
        whenever(mockShareRepo.invitees).thenReturn(MutableLiveData())

        whenever(mockShareRepo.fetchRolesApi(mockShareItem)).then {
            val data = mockShareRepo.fetchRoleItem as MutableLiveData
            data.postValue(mockFetchRolesResponse)
        }

        whenever(mockShareRepo.inviteCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)).then {
            val data = mockShareRepo.inviteCollabsBatch as MutableLiveData
            data.postValue(mockAddCollabsResponse)
        }

        whenever(mockShareRepo.getInviteesApi(mockShareItem, mockFilter)).then {
            val data = mockShareRepo.invitees as MutableLiveData
            data.postValue(mockGetInviteeResponse)
        }
    }

    private fun createExceptions() {

        whenever(mockHttpForbiddenException.responseCode).thenReturn(HttpsURLConnection.HTTP_FORBIDDEN)
        whenever(mockBoxNetworkErrorException.errorType).thenReturn(BoxException.ErrorType.NETWORK_ERROR)

        val boxErrorAlreadyAdded: BoxError = mock()
        whenever(boxErrorAlreadyAdded.code).thenReturn(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR)
        whenever(mockAlreadyCollabException.asBoxError).thenReturn(boxErrorAlreadyAdded)
        whenever(mockAlreadyCollabException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)

        val boxErrorFailedToAdd: BoxError = mock()
        whenever(boxErrorFailedToAdd.code).thenReturn("")
        whenever(mockFailedToAddException.asBoxError).thenReturn(boxErrorFailedToAdd)
        whenever(mockFailedToAddException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)
        whenever(mockBadRequestException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)
    }

    private fun attachObservers() {
        inviteCollabVM.fetchRoleItem.observeForever(mock())
        inviteCollabVM.invitees.observeForever(mock())
        inviteCollabVM.inviteCollabs.observeForever(mock())
    }

    @Test
    fun `test fetch role success`() {
        //configs
        whenever(mockFetchRolesResponse.isSuccess).thenReturn(true)
        whenever(mockFetchRolesResponse.result).thenReturn(mockFetchRoleItemResult)

        //make a network call to fetch roles
        inviteCollabVM.fetchRolesApi(mockShareItem)
        val fetchRoleItemValue = inviteCollabVM.fetchRoleItem.value

        assertEquals(true, fetchRoleItemValue?.isSuccess)
        assertEquals(mockFetchRoleItemResult, fetchRoleItemValue?.data)
    }

    @Test
    fun `test fetch role failure`() {
        //configs
        whenever(mockFetchRolesResponse.isSuccess).thenReturn(false)

        //make a network call to fetch roles
        inviteCollabVM.fetchRolesApi(mockShareItem)
        val fetchRoleItemValue = inviteCollabVM.fetchRoleItem.value

        assertEquals(false, fetchRoleItemValue?.isSuccess)
        assertEquals(R.string.box_sharesdk_network_error, fetchRoleItemValue?.strCode)
    }

    @Test
    fun `test get invitees success`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(true)
        whenever(mockGetInviteeResponse.result).thenReturn(mockGetInviteesResult)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockShareItem, mockFilter)
        val inviteCollabValue = inviteCollabVM.invitees.value

        assertEquals(true, inviteCollabValue?.isSuccess)
        assertEquals(mockGetInviteesResult, inviteCollabValue?.data)
    }

    @Test
    fun `test get invitees failure http forbidden`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockHttpForbiddenException)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockShareItem, mockFilter)
        val inviteCollabValue = inviteCollabVM.invitees.value

        assertEquals(false, inviteCollabValue?.isSuccess)
        assertEquals(R.string.box_sharesdk_insufficient_permissions, inviteCollabValue?.strCode)
    }

    @Test
    fun `test get invitees failure box network error`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockBoxNetworkErrorException)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockShareItem, mockFilter)
        val inviteCollabValue = inviteCollabVM.invitees.value

        assertEquals(false, inviteCollabValue?.isSuccess)
        assertEquals(R.string.box_sharesdk_network_error, inviteCollabValue?.strCode)
    }

    @Test
    fun `test add collab all succeed`() {
        //configs
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createSuccessResponse("user2")
        val boxResponse3 = createSuccessResponse("user3")
        mockAddCollabResult= createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.inviteCollabs.value

        assertEquals(true, addCollabValue?.isSuccess)
        assertEquals(null, addCollabValue?.data) //no submessage for 3 success
        assertEquals(R.string.box_sharesdk_collaborators_invited, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab 1 succeed no failure`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createSuccessResponse(dummyName)
        mockAddCollabResult = createBoxResponseBatch(boxResponse)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.inviteCollabs.value

        assertEquals(true, addCollabValue?.isSuccess)
        assertEquals(dummyName, addCollabValue?.data)
        assertEquals(R.string.box_sharesdk_collaborator_invited, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab succeed 1 already added 1`() {
        //configs
        val dummyName = "user2"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureResponse(dummyName, mockAlreadyCollabException)
        mockAddCollabResult = createBoxResponseBatch(boxResponse, boxResponse2)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.inviteCollabs.value

        assertEquals(true, addCollabValue?.isSuccess) //already added is not considered a failure
        assertEquals(dummyName, addCollabValue?.data)
        assertEquals(R.plurals.box_sharesdk_already_been_invited, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab succeed 1 already add 2`() {
        //configs
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureResponse("user2", mockAlreadyCollabException)
        val boxResponse3 = createFailureResponse("user3", mockAlreadyCollabException)
        mockAddCollabResult= createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.inviteCollabs.value

        assertEquals(true, addCollabValue?.isSuccess)
        assertEquals(2, addCollabValue?.alreadyAdddedCount) //already added 2
        assertEquals(R.plurals.box_sharesdk_already_been_invited, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab succeed 1 failed to add 1`() {
        //configs
        val failedName = "user2"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureResponse("user2", mockFailedToAddException)
        mockAddCollabResult = createBoxResponseBatch(boxResponse, boxResponse2)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.inviteCollabs.value

        assertEquals(false, addCollabValue?.isSuccess)
        assertEquals(failedName, addCollabValue?.data)
        assertEquals(R.string.box_sharesdk_following_collaborators_error, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab succeed 1 failed to add 2`() {
        //configs
        val failedName1 = "user2"
        val failedName2 = "user3"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureResponse(failedName1, mockFailedToAddException)
        val boxResponse3 = createFailureResponse(failedName2, mockFailedToAddException)
        mockAddCollabResult = createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.inviteCollabs.value

        assertEquals(false, addCollabValue?.isSuccess)
        assertEquals("$failedName1 $failedName2", addCollabValue?.data) //appending all failures
        assertEquals(R.string.box_sharesdk_following_collaborators_error, addCollabValue?.strCode)
    }


}