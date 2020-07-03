import StratisSDK
import UIKit

class SettingsViewController: UIViewController {
    public var configuration: Configuration?
    @IBOutlet var statusLabel: UILabel!

    internal var accessToken: String?
    @IBOutlet var accessTokenTextField: UITextField!

    internal var propertyID: String?
    @IBOutlet var propertyIDTextField: UITextField!

    internal var serverEnvironment: ServerEnvironment? {
        didSet {
            serverEnvironmentTextField.text = serverEnvToString(serverEnvironment)
        }
    }

    @IBOutlet var serverEnvironmentTextField: UITextField!
    @IBAction func serverEnvironmentTextFieldTouchDown(_ sender: UITextField) {
        if accessTokenTextField.isFirstResponder { accessTokenTextField.resignFirstResponder() }
        if propertyIDTextField.isFirstResponder { propertyIDTextField.resignFirstResponder() }
        if sender.isFirstResponder { sender.resignFirstResponder() }
        let environmentMenu = UIAlertController(
            title: nil,
            message: "Select a server environment",
            preferredStyle: .actionSheet
        )
        environmentMenu.addAction(UIAlertAction(title: serverEnvToString(ServerEnvironment.DEV), style: .default) { _ in
            self.serverEnvironment = ServerEnvironment.DEV
        })
        environmentMenu.addAction(UIAlertAction(title: serverEnvToString(ServerEnvironment.SANDBOX), style: .default) { _ in
            self.serverEnvironment = ServerEnvironment.SANDBOX
        })
        environmentMenu.addAction(UIAlertAction(title: serverEnvToString(ServerEnvironment.TEST), style: .default) { _ in
            self.serverEnvironment = ServerEnvironment.TEST
        })
        environmentMenu.addAction(UIAlertAction(title: serverEnvToString(ServerEnvironment.PROD), style: .default) { _ in
            self.serverEnvironment = ServerEnvironment.PROD
        })
        environmentMenu.addAction(UIAlertAction(title: "Cancel", style: .cancel))
        present(environmentMenu, animated: true, completion: nil)
    }

    @IBAction func doneButtonTapped(_ sender: UIButton) {
        performSegue(withIdentifier: "dismissSettings", sender: self)
    }

    override func viewDidLoad() {
        super.viewDidLoad()

        accessTokenTextField.delegate = self
        propertyIDTextField.delegate = self

        statusLabel.text = " "
        accessTokenTextField.text = accessToken
        serverEnvironmentTextField.text = serverEnvToString(serverEnvironment)
        propertyIDTextField.text = propertyID
    }

    func showMessage(_ message: Any?) {
        guard let message = message as? String else { return }
        statusLabel.text = message
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            self.statusLabel.text = " "
        }
    }

    func serverEnvToString(_ serverEnv: ServerEnvironment?) -> String {
        switch serverEnv {
        case .DEV:
            return "DEV"
        case .TEST:
            return "TEST"
        case .SANDBOX:
            return "SANDBOX"
        case .PROD:
            return "PROD"
        default:
            return ""
        }
    }
}

extension SettingsViewController: UITextFieldDelegate {
    func textFieldDidEndEditing(_ textField: UITextField) {
        if textField == accessTokenTextField {
            accessToken = accessTokenTextField.text?.trimmingCharacters(in: .whitespaces)
            showMessage("Set Access Token: \(accessToken ?? "")")
        } else if textField == propertyIDTextField {
            propertyID = propertyIDTextField.text?.trimmingCharacters(in: .whitespaces)
            showMessage("Set Property ID: \(propertyID ?? "")")
        }
    }

    func textFieldShouldReturn(_ textField: UITextField) -> Bool {
        if let nextResponder = textField.superview?.viewWithTag(textField.tag + 1) {
            if nextResponder == serverEnvironmentTextField {
                textField.resignFirstResponder()
                serverEnvironmentTextFieldTouchDown(serverEnvironmentTextField)
            } else {
                nextResponder.becomeFirstResponder()
            }
        } else {
            textField.resignFirstResponder()
        }
        return true
    }
}
