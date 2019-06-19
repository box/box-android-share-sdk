package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.models.*
import com.box.androidsdk.content.requests.BoxRequestsShare
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.box.androidsdk.share.R
import com.box.androidsdk.share.api.ShareController
import com.box.androidsdk.share.internal.models.BoxIteratorInvitees
import com.box.androidsdk.share.sharerepo.BaseShareRepo
import com.box.androidsdk.share.sharerepo.ShareRepo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
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


    private val shareController: ShareController = mock()

    private val mockBoxCollaborationItem: BoxCollaborationItem = mock()
    private val mockEmailList: Array<String> = arrayOf("boxuser@box.com", "boxuser2@box.com")
    private val mockSelectedRole: BoxCollaboration.Role = BoxCollaboration.Role.EDITOR
    private val mockFilter: String = "filter"
    private val mockShareItem: BoxCollaborationItem = mock()

    private val mockGetInviteeResponseTask: BoxFutureTask<BoxIteratorInvitees> = mock()
    private val mockGetInviteeResponse: BoxResponse<BoxIteratorInvitees> = mock()
    private val mockFetchRolesResponseTask: BoxFutureTask<BoxCollaborationItem> = mock()
    private val mockFetchRolesResponse: BoxResponse<BoxCollaborationItem> = mock()
    private val mockAddCollabsResponseTask: BoxFutureTask<BoxResponseBatch> = mock()
    private val mockAddCollabsResponse: BoxResponse<BoxResponseBatch> = mock()

    private val mockFailedToAddException: BoxException = mock()
    private val mockHttpForbiddenException: BoxException = mock()
    private val mockBoxNetworkErrorException: BoxException = mock()
    private val mockAlreadyAddedCollabException: BoxException = mock()
    private val mockBadRequestException: BoxException = mock()

    private val mockGetInviteesResult: BoxIteratorInvitees = mock()
    private val mockFetchRoleItemResult: BoxCollaborationItem = mock()
    private val mockAddCollabResult: BoxResponseBatch = mock()

    private lateinit var shareRepo: BaseShareRepo

    private lateinit var inviteCollabVM: InviteCollaboratorsVM

    @Before
    fun setup() {

        whenever(shareController.getInvitees(mockShareItem, mockFilter)).thenReturn(mockGetInviteeResponseTask)
        whenever(shareController.fetchRoles(mockShareItem)).thenReturn(mockFetchRolesResponseTask)
        whenever(shareController.addCollaborations(mockShareItem, mockSelectedRole, mockEmailList)).thenReturn(mockAddCollabsResponseTask)

        shareRepo = ShareRepo(shareController)
        inviteCollabVM = InviteCollaboratorsVM(shareRepo, mockShareItem)

        attachObservers()
        createExceptions()
        createStubs()
    }

    private fun createExceptions() {

        whenever(mockHttpForbiddenException.responseCode).thenReturn(HttpsURLConnection.HTTP_FORBIDDEN)
        whenever(mockBoxNetworkErrorException.errorType).thenReturn(BoxException.ErrorType.NETWORK_ERROR)

        val boxErrorAlreadyAdded: BoxError = mock()
        whenever(boxErrorAlreadyAdded.code).thenReturn(BoxRequestsShare.AddCollaboration.ERROR_CODE_USER_ALREADY_COLLABORATOR)
        whenever(mockAlreadyAddedCollabException.asBoxError).thenReturn(boxErrorAlreadyAdded)
        whenever(mockAlreadyAddedCollabException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST) //not sure it it is bad request but good enough for testing

        val boxErrorFailedToAdd: BoxError = mock()
        whenever(boxErrorFailedToAdd.code).thenReturn("")
        whenever(mockFailedToAddException.asBoxError).thenReturn(boxErrorFailedToAdd)
        whenever(mockFailedToAddException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST) //not sure if it is bad request but good enough for testing
        whenever(mockBadRequestException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)
    }

    private fun attachObservers() {
        inviteCollabVM.fetchRoleItem.observeForever(mock())
        inviteCollabVM.invitees.observeForever(mock())
        inviteCollabVM.addCollabs.observeForever(mock())
    }

    private fun createStubs() {
        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>
            callback.onCompleted(mockGetInviteeResponse)
            null
        }.whenever(mockGetInviteeResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxIteratorInvitees>>())
        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxCollaborationItem>
            callback.onCompleted(mockFetchRolesResponse)
            null
        }.whenever(mockFetchRolesResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxCollaborationItem>>())

        doAnswer {
            val callback = it.arguments[0] as BoxFutureTask.OnCompletedListener<BoxResponseBatch>
            callback.onCompleted(mockAddCollabsResponse)
            null
        }.whenever(mockAddCollabsResponseTask).addOnCompletedListener(any<BoxFutureTask.OnCompletedListener<BoxResponseBatch>>())
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

        //VM reacts by updating its LiveData correctly
        assertEquals(mockFetchRoleItemResult, inviteCollabVM.fetchRoleItem.value?.data)
    }

    @Test
    fun `test fetch role failure`() {
        //configs
        whenever(mockFetchRolesResponse.isSuccess).thenReturn(false)

        //make a network call to fetch roles
        inviteCollabVM.fetchRolesApi(mockShareItem)

        //should be null since the request was a failure
        assertEquals(null, inviteCollabVM.fetchRoleItem.value?.data, null)

        //associated failure message
        assertEquals(R.string.box_sharesdk_network_error, inviteCollabVM.fetchRoleItem.value?.strCode)
    }

    @Test
    fun `test get invitees success`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(true)
        whenever(mockGetInviteeResponse.result).thenReturn(mockGetInviteesResult)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockShareItem, mockFilter)

        //VM reacts by updating its LiveData correctly
        assertEquals(mockGetInviteesResult, inviteCollabVM.invitees.value?.data)
    }

    @Test
    fun `test get invitees failure http forbidden`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockHttpForbiddenException)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockShareItem, mockFilter)

        //should be null since the request was a failure
        assertEquals(null, inviteCollabVM.invitees.value?.data)

        //associated error message
        assertEquals(R.string.box_sharesdk_insufficient_permissions, inviteCollabVM.invitees.value?.strCode)
    }

    @Test
    fun `test get invitees failure box network error`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockBoxNetworkErrorException)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockShareItem, mockFilter)

        //should be null since the request was a failure
        assertEquals(null, inviteCollabVM.invitees.value?.data)

        //associated error message
        assertEquals(R.string.box_sharesdk_network_error, inviteCollabVM.invitees.value?.strCode)
    }


    @Test
    fun `test update failure stats already added case`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createFailureException(dummyName, mockAlreadyAddedCollabException)
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

        //update stats
        inviteCollabVM.updateFailureStats(boxResponse, failedCollaboratorList)

        //the names should be equal since dummy name was already added.
        assertEquals(dummyName, failedCollaboratorList[0])
        assertEquals(1, failedCollaboratorList.size)

        //simulate second failure
        inviteCollabVM.updateFailureStats(boxResponse, failedCollaboratorList)

        //more failure should update the size to 2
        assertEquals(2, failedCollaboratorList.size)
    }

    @Test
    fun `test process request success size 1 non null`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createSuccessResponse(dummyName)

        val boxResponses = createBoxResponseBatch(boxResponse)

        //process request
        val result = inviteCollabVM.processRequestSuccess(boxResponses)

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
        val result = inviteCollabVM.processRequestSuccess(boxResponses)

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
        val result = inviteCollabVM.processRequestSuccess(boxResponses)

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
        val result = inviteCollabVM.processRequestFailure(failedCollabs, dummyName, alreadyAddedCount)

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
        val result = inviteCollabVM.processRequestFailure(failedCollabs, dummyName, alreadyAddedCount)

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
        val result = inviteCollabVM.processRequestFailure(failedCollabs, dummyName, alreadyAddedCount)

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
        val result = inviteCollabVM.processRequestFailure(failedCollabs, dummyName, alreadyAddedCount)

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
        val boxResponseBatch = createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockAddCollabsResponse.result).thenReturn(boxResponseBatch)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val res = inviteCollabVM.addCollabs

        assertEquals(true, res.value?.isSuccess )
        assertEquals(null, res.value?.mData ) //no submessage for 3 success
        assertEquals(R.string.box_sharesdk_collaborators_invited, res.value?.strCode)
    }

    @Test
    fun `test add collab 1 succeed no failure`() {
        //configs
        val dummyName = "Box User"
        val boxResponse = createSuccessResponse(dummyName)
        val boxResponseBatch = createBoxResponseBatch(boxResponse)
        whenever(mockAddCollabsResponse.result).thenReturn(boxResponseBatch)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val res = inviteCollabVM.addCollabs

        assertEquals(true, res.value?.isSuccess )
        assertEquals(dummyName, res.value?.mData )
        assertEquals(R.string.box_sharesdk_collaborator_invited, res.value?.strCode)
    }

    @Test
    fun `test add collab succeed 1 already added 1`() {
        //configs
        val dummyName = "user2"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureException(dummyName, mockAlreadyAddedCollabException)
        val boxResponseBatch = createBoxResponseBatch(boxResponse, boxResponse2)
        whenever(mockAddCollabsResponse.result).thenReturn(boxResponseBatch)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val res = inviteCollabVM.addCollabs

        assertEquals(true, res.value?.isSuccess) //already added is not considered a failure
        assertEquals(dummyName, res.value?.mData)
        assertEquals(R.string.box_sharesdk_has_already_been_invited, res.value?.strCode)
    }

    @Test
    fun `test add collab succeed 1 already add 2`() {
        //configs
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureException("user2", mockAlreadyAddedCollabException)
        val boxResponse3 = createFailureException("user3", mockAlreadyAddedCollabException)
        val boxResponseBatch = createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockAddCollabsResponse.result).thenReturn(boxResponseBatch)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val res = inviteCollabVM.addCollabs

        assertEquals(true, res.value?.isSuccess)
        assertEquals(Integer.toString(2), res.value?.mData) //already added 2
        assertEquals(R.string.box_sharesdk_num_has_already_been_invited, res.value?.strCode)
    }

    @Test
    fun `test add collab succeed 1 failed to add 1`() {
        //configs
        val failedName = "user2"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureException("user2", mockFailedToAddException)
        val boxResponseBatch = createBoxResponseBatch(boxResponse, boxResponse2)
        whenever(mockAddCollabsResponse.result).thenReturn(boxResponseBatch)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val res = inviteCollabVM.addCollabs

        assertEquals(false, res.value?.isSuccess)
        assertEquals(failedName, res.value?.mData)
        assertEquals(R.string.box_sharesdk_following_collaborators_error, res.value?.strCode)
    }

    @Test
    fun `test add collab succeed 1 failed to add 2`() {
        //configs
        val failedName1 = "user2"
        val failedName2 = "user3"
        val boxResponse = createSuccessResponse("user1")
        val boxResponse2 = createFailureException(failedName1, mockFailedToAddException)
        val boxResponse3 = createFailureException(failedName2, mockFailedToAddException)
        val boxResponseBatch = createBoxResponseBatch(boxResponse, boxResponse2, boxResponse3)
        whenever(mockAddCollabsResponse.result).thenReturn(boxResponseBatch)

        //process request
        inviteCollabVM.addCollabsApi(mockShareItem, mockSelectedRole, mockEmailList)
        val res = inviteCollabVM.addCollabs

        assertEquals(false, res.value?.isSuccess)
        assertEquals("$failedName1 $failedName2", res.value?.mData) //appending all failures
        assertEquals(R.string.box_sharesdk_following_collaborators_error, res.value?.strCode)
    }


}