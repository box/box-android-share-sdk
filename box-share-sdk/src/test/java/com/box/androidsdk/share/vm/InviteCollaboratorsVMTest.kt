package com.box.androidsdk.share.vm

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.BoxFutureTask
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxCollaborationItem
import com.box.androidsdk.content.models.BoxError
import com.box.androidsdk.content.models.BoxUser
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
    private val mockmShareItem: BoxCollaborationItem = mock()

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

        whenever(shareController.getInvitees(mockmShareItem, mockFilter)).thenReturn(mockGetInviteeResponseTask)
        whenever(shareController.fetchRoles(mockmShareItem)).thenReturn(mockFetchRolesResponseTask)
        whenever(shareController.addCollaborations(mockmShareItem, mockSelectedRole, mockEmailList)).thenReturn(mockAddCollabsResponseTask)

        shareRepo = ShareRepo(shareController)
        inviteCollabVM = InviteCollaboratorsVM(shareRepo, mockmShareItem)

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
        val boxErrorFailedToAdd: BoxError = mock()
        whenever(boxErrorFailedToAdd.code).thenReturn("")
        whenever(mockFailedToAddException.asBoxError).thenReturn(boxErrorFailedToAdd)
        whenever(mockBadRequestException.responseCode).thenReturn(HttpsURLConnection.HTTP_BAD_REQUEST)
    }

    private fun attachObservers() {
        inviteCollabVM.fetchRoleItem.observeForever(mock())
        inviteCollabVM.invitees.observeForever(mock())
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

    @Test
    fun `test fetch role success`() {
        //configs
        whenever(mockFetchRolesResponse.isSuccess).thenReturn(true)
        whenever(mockFetchRolesResponse.result).thenReturn(mockFetchRoleItemResult)

        //make a network call to fetch roles
        inviteCollabVM.fetchRolesApi(mockmShareItem)

        //VM reacts by updating its LiveData correctly
        assertEquals(inviteCollabVM.fetchRoleItem.value?.data, mockFetchRoleItemResult)
    }

    @Test
    fun `test fetch role failure`() {
        //configs
        whenever(mockFetchRolesResponse.isSuccess).thenReturn(false)

        //make a network call to fetch roles
        inviteCollabVM.fetchRolesApi(mockmShareItem)

        //should be null since the request was a failure
        assertEquals(inviteCollabVM.fetchRoleItem.value?.data, null)

        //associated failure message
        assertEquals(inviteCollabVM.fetchRoleItem.value?.strCode, R.string.box_sharesdk_network_error)
    }

    @Test
    fun `test get invitees success`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(true)
        whenever(mockGetInviteeResponse.result).thenReturn(mockGetInviteesResult)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockmShareItem, mockFilter)

        //VM reacts by updating its LiveData correctly
        assertEquals(inviteCollabVM.invitees.value?.data, mockGetInviteesResult)
    }

    @Test
    fun `test get invitees failure http forbidden`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockHttpForbiddenException)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockmShareItem, mockFilter)

        //should be null since the request was a failure
        assertEquals(inviteCollabVM.invitees.value?.data, null)

        //associated error message
        assertEquals(inviteCollabVM.invitees.value?.strCode, R.string.box_sharesdk_insufficient_permissions)
    }

    @Test
    fun `test get invitees failure box network error`() {
        //configs
        whenever(mockGetInviteeResponse.isSuccess).thenReturn(false)
        whenever(mockGetInviteeResponse.exception).thenReturn(mockBoxNetworkErrorException)

        //make a network call to fetch roles
        inviteCollabVM.getInviteesApi(mockmShareItem, mockFilter)

        //should be null since the request was a failure
        assertEquals(inviteCollabVM.invitees.value?.data, null)

        //associated error message
        assertEquals(inviteCollabVM.invitees.value?.strCode, R.string.box_sharesdk_network_error)
    }

    @Test
    fun `test update failure stats already added case`() {
        //configs
        val boxResponse: BoxResponse<BoxCollaboration> = mock()
        whenever(boxResponse.exception).thenReturn(mockAlreadyAddedCollabException)
        val boxUser: BoxUser = mock()
        val dummyName = "Box User"
        whenever(boxUser.login).thenReturn(dummyName)
        val boxRequestShare: BoxRequestsShare.AddCollaboration = mock()
        whenever(boxRequestShare.accessibleBy).thenReturn(boxUser)
        whenever(boxResponse.request).thenReturn(boxRequestShare)
        val failedCollaboratorList = arrayListOf<String>()

        //update stats
        val name = inviteCollabVM.updateFailureStats(boxResponse, failedCollaboratorList)

        //the names should be equal since dummy name was already added.
        assertEquals(name, dummyName)
        assertEquals(failedCollaboratorList.size, 0)
    }

    @Test
    fun `test update failure stats failed to add collaborator`() {
        //configs
        val boxResponse: BoxResponse<BoxCollaboration> = mock()
        whenever(boxResponse.exception).thenReturn(mockFailedToAddException)
        val boxUser: BoxUser = mock()
        val dummyName = "Box User"
        whenever(boxUser.login).thenReturn(dummyName)
        val boxRequestShare: BoxRequestsShare.AddCollaboration = mock()
        whenever(boxRequestShare.accessibleBy).thenReturn(boxUser)
        whenever(boxResponse.request).thenReturn(boxRequestShare)
        val failedCollaboratorList = arrayListOf<String>()

        //update stats
        inviteCollabVM.updateFailureStats(boxResponse, failedCollaboratorList)

        //the names should be equal since dummy name was already added.
        assertEquals(failedCollaboratorList[0], dummyName)
        assertEquals(failedCollaboratorList.size, 1)

        inviteCollabVM.updateFailureStats(boxResponse, failedCollaboratorList)

        //more failure should update the size to 2
        assertEquals(failedCollaboratorList.size, 2)
    }
}