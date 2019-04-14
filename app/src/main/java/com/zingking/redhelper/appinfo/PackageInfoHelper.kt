package com.zingking.redhelper.appinfo

/**
 * Copyright © 2018 www.zingking.cn All Rights Reserved.
 * @author Z.kai
 * @date 2019/4/12
 * @description
 */

class PackageInfoHelper private constructor() {

    companion object {
        val instance = PackageInfoHelperHolder.packageInfoHelper
    }

    var packageInfoMap: HashMap<String, IPackageInfo> = hashMapOf()

    private object PackageInfoHelperHolder {
        val packageInfoHelper = PackageInfoHelper()
    }

    fun addPackageInfo(packageName: String, packageInfo: IPackageInfo) {
        packageInfoMap.put(packageName, packageInfo)
    }

    fun removePackageInfo(packageName: String): IPackageInfo? {
        return packageInfoMap.remove(packageName)
    }

    fun getPackageInfo(packageName: String): IPackageInfo? {
        return packageInfoMap.get(packageName)
    }
}