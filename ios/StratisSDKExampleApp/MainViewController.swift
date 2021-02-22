import StratisSDK
import UIKit

class MainViewController: UIViewController {
    @IBOutlet var statusLabel: UILabel!
    @IBOutlet var tableView: UITableView!
    @IBOutlet var loadingView: UIView!
    public var stratisSDK: StratisSDK?
    private var locks = [StratisLock]()
    public var accessToken: String = ""
    public var propertyID: String = ""

    override func viewDidLoad() {
        super.viewDidLoad()

        statusLabel.text = " "
        tableView.delegate = self
        tableView.dataSource = self

        let configuration = Configuration(
            serverEnvironment: .DEV,
            accessToken: accessToken,
            propertyID: propertyID,
            remoteLoggingEnabled: true,
            loggingMetadata: ["app": "SDK Example App"]
        )
        stratisSDK = StratisSDK(configuration: configuration)
        stratisSDK?.deviceAccessDelegate = self
        stratisSDK?.deviceDiscoveryDelegate = self
    }

    @IBAction func unwind(segue: UIStoryboardSegue) {
        var loggingMetadata = ["app": "SDK Example App"]
        if let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String {
            loggingMetadata["version"] = version
        }
        guard let sourceVC = segue.source as? SettingsViewController else { return }
        guard let serverEnvironment = sourceVC.serverEnvironment else { return }
        guard let accessToken = sourceVC.accessToken else { return }
        if let propertyID = sourceVC.propertyID {
            self.propertyID = propertyID
        }
        let configuration = Configuration(
            serverEnvironment: serverEnvironment,
            accessToken: accessToken,
            propertyID: propertyID,
            remoteLoggingEnabled: true,
            loggingMetadata: loggingMetadata
        )
        stratisSDK = StratisSDK(configuration: configuration)
    }

    @IBOutlet var fetchAccessibleDevicesButton: UIButton!
    @IBAction func fetchAccessibleDevicesButtonTapped(_ sender: UIButton) {
        fetchAccessibleDevicesButton.makeEnabled(false)
        stratisSDK?.fetchAccessibleDevices()
    }

    @IBOutlet var discoverDevicesButton: UIButton!
    @IBAction func discoverDevicesButtonTapped(_ sender: UIButton) {
        stratisSDK?.discoverActionableDevices(locks)
        discoverDevicesButton.makeEnabled(false)
    }

    func showMessage(_ message: Any?) {
        guard let message = message as? String else { return }
        statusLabel.text = message
        DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) {
            self.statusLabel.text = " "
        }
    }

    func setIsLoading(_ loading: Bool) {
        let isLoading = view.subviews.contains(loadingView)
        if loading, !isLoading {
            loadingView.frame = view.bounds
            view.addSubview(loadingView)
            view.bringSubviewToFront(loadingView)
        } else if !loading, isLoading {
            loadingView.removeFromSuperview()
        }
    }
}

extension MainViewController: StratisDeviceAccessDelegate {
    func stratisDeviceAccessRequestCompleted(_ stratisSDK: StratisSDK, devices: [StratisLock], error: StratisError?) {
        DispatchQueue.main.async { [weak self] in
            self?.fetchAccessibleDevicesButton.makeEnabled(true)
            self?.locks = devices
            self?.tableView.reloadData()
            if error != nil {
                self?.showMessage("There was an error getting devices.")
            }
        }
    }
}

extension MainViewController: StratisDeviceDiscoveryDelegate {
    func stratisDiscoveryUpdatedRSSI(_ stratisSDK: StratisSDK, devices: [StratisLock]) {
        print(devices)
    }
    
    func stratisDiscoveryDevicesOutOfRange(_ stratisSDK: StratisSDK, devices: [StratisLock]) {
        print(devices)
    }
    
    func stratisDiscoveredDevices(_ stratisSDK: StratisSDK, devices: [StratisLock]) {
        DispatchQueue.main.async { [weak self] in
            self?.tableView.reloadData()
        }
    }

    func stratisDiscoveryEncounteredError(_ stratisSDK: StratisSDK, error: StratisError) {
        DispatchQueue.main.async { [weak self] in
            self?.showMessage(error.debugMessage)
        }
    }

    func stratisDiscoveryCompleted(_ stratisSDK: StratisSDK) {
        DispatchQueue.main.async { [weak self] in
            self?.discoverDevicesButton.makeEnabled(true)
        }
    }
}

extension MainViewController: StratisDeviceActivationDelegate {
    func stratisDeviceActivationDidPostEvent(_ event: StratisLock.ActivationEvent, forDevice device: StratisLock, withError error: StratisError?) {
        DispatchQueue.main.async { [weak self] in
            if let error = error {
                self?.showMessage("Event \(event) posted with error for lock \(device.name), error: \(error.localizedDescription)")
            } else {
                switch event {
                case .activationStarted:
                    self?.showMessage("activation started")
                case .presentDeviceToLock:
                    self?.showMessage("present dormakaba instructions")
                case .activationComplete:
                    self?.showMessage("Successfully activated lock \(device.name)")
                }
            }
        }
    }
}

extension MainViewController: UITableViewDelegate {
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
        let lock = locks[indexPath.section]
        showMessage("Activating lock \(lock.name) ...")
        lock.activationDelegate = self
        lock.activate()
        tableView.deselectRow(at: indexPath, animated: true)
    }
}

extension MainViewController: UITableViewDataSource {
    func tableView(_ tableView: UITableView, viewForHeaderInSection section: Int) -> UIView? {
        let view = UIView()
        view.backgroundColor = .clear
        return view
    }

    func numberOfSections(in tableView: UITableView) -> Int {
        return locks.count
    }

    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return 1
    }

    func tableView(_ tableView: UITableView, heightForHeaderInSection section: Int) -> CGFloat {
        return 12
    }

    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "lockCellView")!
        if let lockTableCell = cell as? LockTableViewCell {
            lockTableCell.configureFor(lock: locks[indexPath.section])
        }
        return cell
    }
}

class LockTableViewCell: UITableViewCell {
    @IBOutlet var lockView: UIView!
    @IBOutlet var lockNameLabel: UILabel!
    @IBOutlet var lockModelLabel: UILabel!
    @IBOutlet var rssiLabel: UILabel!

    func configureFor(lock: StratisLock) {
        let isEnabled = lock.actionable
        if let lock = lock as? BLELock {
            rssiLabel.text = lock.rssi?.stringValue
        }
        lockNameLabel.alpha = isEnabled ? 1.0 : 0.35
        lockModelLabel.alpha = isEnabled ? 1.0 : 0.35
        lockNameLabel.text = lock.name
        lockModelLabel.text = lock.model
    }
}
