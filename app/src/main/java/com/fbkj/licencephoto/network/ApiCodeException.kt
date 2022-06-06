package com.fbkj.licencephoto.network

/**
 * api 接口处理失败返回 code != 0 时抛出
 */
class ApiCodeException : Exception {

    var code: Int = 0
        private set

    constructor(code: Int) {
        this.code = code
    }

    constructor(code: Int, message: String?) : super(message) {
        this.code = code
    }

}