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
import com.box.androidsdk.share.sharerepo.BaseShareRepo
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

    private val mockShareRepo: BaseShareRepo = mock()

    private lateinit var inviteCollabVM: InviteCollaboratorsVM

    @Before
    fun setup() {
        mockShareRepo()

        inviteCollabVM = InviteCollaboratorsVM(mockShareRepo, mockShareItem)

        attachObservers()
        createExceptions()
    }

    private fun mockShareRepo() {
        whenever(mockShareRepo.fetchRoleItem).thenReturn(MutableLiveData())
        whenever(mockShareRepo.addCollabsBatch).thenReturn(MutableLiveData())
        whenever(mockShareRepo.invitees).thenReturn(MutableLiveData())

        whenever(mockShareRepo.fetchRolesApi(mockShareItem)).then {
            val data = mockShareRepo.fetchRoleItem as MutableLiveData
            data.postValue(mockFetchRolesResponse)
        }

        whenever(mockShareRepo.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)).then {
            val data = mockShareRepo.addCollabsBatch as MutableLiveData
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
        inviteCollabVM.addCollabs.observeForever(mock())
    }

    private fun createFailureException(dummyName: String, exception: BoxException): BoxResponse<BoxCollaboration> {
        val boxResponse: BoxResponse<BoxCollaboration> = mock()
        whenever(boxResponse.exception).thenReturn(exception)
        val boxUser: BoxUser = mock()

        whenever(boxUser.login).thenReturn(dummyName)
        val boxRequestShare: BoxRequestsShare.AddCollaboration = mock()
        whenever(boxRequestShare.accessibleBy).thenReturn(boxUser)
        whenever(boxResponse.request).thenReturn(boxRequestShare)
        whenever(boxResponse.isSuccess).thenReturn(false)
        return boxResponse
    }

    private fun createSuccessResponse(dummyName: String?): BoxResponse<BoxCollaboration> {
        val boxResponse: BoxResponse<BoxCollaboration> = mock()
        var boxUser: BoxUser? = null
        if (dummyName != null) {
            boxUser = mock()
            whenever(boxUser.login).thenReturn(dummyName)
        }
        val boxRequestShare: BoxCollaboration = mock()
        whenever(boxRequestShare.accessibleBy).thenReturn(boxUser)
        whenever(boxResponse.result).thenReturn(boxRequestShare)
        whenever(boxResponse.isSuccess).thenReturn(true)
        return boxResponse
    }

    private fun createBoxResponseBatch(vararg respones: BoxResponse<BoxCollaboration>): BoxResponseBatch {
        val boxResponses: BoxResponseBatch = mock()
        val boxResponseList = arrayListOf<BoxResponse<BoxObject>>()
        respones.forEach { boxResponse ->  boxResponseList.add(boxResponse as BoxResponse<BoxObject>)}
        whenever(boxResponses.responses).thenReturn(boxResponseList)
        return boxResponses
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
    fun `test update failure stats already added case`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createFailureException(dummyName, mockAlreadyCollabException)
        val failedCollaboratorList = arrayListOf<String>()

        //update stats
        val name = inviteCollabVM.updateFailureStats(boxResponse, failedCollaboratorList)

        //the names should be equal since dummy name was already added.
        assertEquals(dummyName, name)
        assertEquals(0, failedCollaboratorList.size)
    }


    @Test
    fun `test update failure stats failed to add collaborator`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createFailureException(dummyName, mockFailedToAddException)
        val failedCollaboratorList = arrayListOf<String>()

        //simulating failing multiple times correctly updating failedCollabList
        for (i in 1..3) {
            inviteCollabVM.updateFailureStats(boxResponse, failedCollaboratorList)
            assertEquals(dummyName, failedCollaboratorList[i - 1])
            assertEquals(i, failedCollaboratorList.size)
        }
    }

    @Test
    fun `test process request success size 1 non null`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createSuccessResponse(dummyName)

        val boxResponses = createBoxResponseBatch(boxResponse)

        //process request
        val result = inviteCollabVM.processAddCollabsRequestSuccess(boxResponses)

        //compare values
        assertEquals(R.string.box_sharesdk_collaborator_invited, Integer.parseInt(result[0]))
        assertEquals(dummyName, result[1])
    }

    @Test
    fun `test process request success size 1 getAccessibly by is null`() {
        //configs
        val dummyName = null
        val boxResponse = createSuccessResponse(dummyName)

        val boxResponses = createBoxResponseBatch(boxResponse)

        //process request
        val result = inviteCollabVM.processAddCollabsRequestSuccess(boxResponses)

        //compare values
        assertEquals(R.string.box_sharesdk_collaborators_invited, Integer.parseInt(result[0]))
        assertEquals(null, result[1])
    }

    @Test
    fun `test process request success greater than 1`() {
        //configs
        val dummyName = "Box User"
        val boxResponse: BoxResponse<BoxCollaboration> = createSuccessResponse(dummyName)
        val boxResponse2: BoxResponse<BoxCollaboration> = createSuccessResponse(dummyName+"2")
        val boxResponses = createBoxResponseBatch(boxResponse, boxResponse2)

        //process request
        val result = inviteCollabVM.processAddCollabsRequestSuccess(boxResponses)

        //compare values
        assertEquals(R.string.box_sharesdk_collaborators_invited, Integer.parseInt(result[0]))
        assertEquals(null, result[1])
    }

    @Test
    fun `test process request failed collaborator non empty`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 1
        val failedCollabs = arrayListOf<String>()
        failedCollabs.add("failure 1")
        failedCollabs.add("failure 2")
        val res = "failure 1 failure 2"


        //process request
        val result = inviteCollabVM.processAddCollabsRequestFailure(failedCollabs, dummyName, alreadyAddedCount)

        //compare values (ignored already added since failure to add is more important)
        assertEquals(R.string.box_sharesdk_following_collaborators_error, Integer.parseInt(result[0]))
        assertEquals(res, result[1])

    }

    @Test
    fun `test process request failed collaborator empty alreadyAddedCount 1`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 1
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = inviteCollabVM.processAddCollabsRequestFailure(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_has_already_been_invited, Integer.parseInt(result[0]))
        assertEquals(dummyName, result[1])

    }

    @Test
    fun `test process request failed collaborator empty alreadyAddedCount greater than 1`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 2
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = inviteCollabVM.processAddCollabsRequestFailure(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_num_has_already_been_invited, Integer.parseInt(result[0]))
        assertEquals(Integer.toString(alreadyAddedCount), result[1])

    }

    @Test
    fun `test process request failed collaborator empty alreadyAddedCount 0`() {
        //configs
        val dummyName = "already added user"
        val alreadyAddedCount = 0
        val failedCollabs = arrayListOf<String>()

        //process request
        val result = inviteCollabVM.processAddCollabsRequestFailure(failedCollabs, dummyName, alreadyAddedCount)

        //compare values
        assertEquals(R.string.box_sharesdk_unable_to_invite, Integer.parseInt(result[0]))
        assertEquals(null, result[1])

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
        val addCollabValue = inviteCollabVM.addCollabs.value

        assertEquals(true, addCollabValue?.isSuccess)
        assertEquals(null, addCollabValue?.mData) //no submessage for 3 success
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
        val addCollabValue = inviteCollabVM.addCollabs.value

        assertEquals(true, addCollabValue?.isSuccess)
        assertEquals(dummyName, addCollabValue?.mData)
        assertEquals(R.string.box_sharesdk_collaborator_invited, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab succeed 1 already added 1`() {
        //configs
        val dummyName = "user2"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureException(dummyName, mockAlreadyCollabException)
        mockAddCollabResult = createBoxResponseBatch(boxResponse, boxResponse2)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.addCollabs.value

        assertEquals(true, addCollabValue?.isSuccess) //already added is not considered a failure
        assertEquals(dummyName, addCollabValue?.mData)
        assertEquals(R.string.box_sharesdk_has_already_been_invited, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab succeed 1 already add 2`() {
        //configs
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureException("user2", mockAlreadyCollabException)
        val boxResponse3 = createFailureException("user3", mockAlreadyCollabException)
        mockAddCollabResult= createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.addCollabs.value

        assertEquals(true, addCollabValue?.isSuccess)
        assertEquals(Integer.toString(2), addCollabValue?.mData) //already added 2
        assertEquals(R.string.box_sharesdk_num_has_already_been_invited, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab succeed 1 failed to add 1`() {
        //configs
        val failedName = "user2"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureException("user2", mockFailedToAddException)
        mockAddCollabResult = createBoxResponseBatch(boxResponse, boxResponse2)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.addCollabs.value

        assertEquals(false, addCollabValue?.isSuccess)
        assertEquals(failedName, addCollabValue?.mData)
        assertEquals(R.string.box_sharesdk_following_collaborators_error, addCollabValue?.strCode)
    }

    @Test
    fun `test add collab succeed 1 failed to add 2`() {
        //configs
        val failedName1 = "user2"
        val failedName2 = "user3"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureException(failedName1, mockFailedToAddException)
        val boxResponse3 = createFailureException(failedName2, mockFailedToAddException)
        mockAddCollabResult = createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockAddCollabsResponse.result).thenReturn(mockAddCollabResult)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val addCollabValue = inviteCollabVM.addCollabs.value

        assertEquals(false, addCollabValue?.isSuccess)
        assertEquals("$failedName1 $failedName2", addCollabValue?.mData) //appending all failures
        assertEquals(R.string.box_sharesdk_following_collaborators_error, addCollabValue?.strCode)
    }


}