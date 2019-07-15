package com.box.androidsdk.share.utils

import com.box.androidsdk.content.BoxException
import com.box.androidsdk.content.models.BoxCollaboration
import com.box.androidsdk.content.models.BoxObject
import com.box.androidsdk.content.models.BoxUser
import com.box.androidsdk.content.requests.BoxRequestsShare
import com.box.androidsdk.content.requests.BoxResponse
import com.box.androidsdk.content.requests.BoxResponseBatch
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever

class ResponsesCreator {
    companion object {
        fun createFailureResponse(dummyName: String, exception: BoxException): BoxResponse<BoxCollaboration> {
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

        fun createSuccessResponse(dummyName: String?): BoxResponse<BoxCollaboration> {
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

        fun createBoxResponseBatch(vararg respones: BoxResponse<BoxCollaboration>): BoxResponseBatch {
            val boxResponses: BoxResponseBatch = mock()
            val boxResponseList = arrayListOf<BoxResponse<BoxObject>>()
            respones.forEach { boxResponse -> boxResponseList.add(boxResponse as BoxResponse<BoxObject>) }
            whenever(boxResponses.responses).thenReturn(boxResponseList)
            return boxResponses
        }
    }
}
