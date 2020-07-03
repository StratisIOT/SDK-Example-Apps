//
//  UIButtonExtensions.swift
//  StratisSDK_Example
//
//  Created by Stratis on 9/10/19.
//  Copyright © 2019 Stratis. All rights reserved.
//

import Foundation
import UIKit

extension UIButton {
    /// Can be used to toggle the enabled state, accompanied by an opacity fade when disabled
    func makeEnabled(_ enabled: Bool) {
        isEnabled = enabled
        alpha = enabled ? 1.0 : 0.4
    }
}
