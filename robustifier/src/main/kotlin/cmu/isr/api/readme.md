# LTS-Robustness API

Author: William Morris

This API has been created to allow for easy access to LTS-robustness's robustness calculation features
without going through the CLI.

Further additions to this API are welcomed as they're needed.

## Calculate Delta

This function calculates the robustness $\Delta$ of an environment (without env properties), a controller, and a safety property.

## Parameters:
* Compact LTS for the environment
* Compact LTS for the system
* Compact LTS for the system safety property

## Returns:
* \Delta, the robustness set of the system.