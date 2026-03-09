## Overview

This document describes the intended functionality of the PetClinic Android app.

## Tech stack

- "Black box" API. See: [PET_CLINIC_API_REFERENCE.md](../../PET_CLINIC_API_REFERENCE.md) for a comprehensive view into what the API is expected to provide
- OkHttp3 for networked calls

## Features

### Nav bar

The app should be separated into different core screens. We will use Navigation with Compose to create a Nav Bar. The Nav Bar should be its own Fragment.

### Home screen

A simple home screen that maybe has some blurb about what this app is (it's the Application Signals demo app). Include a picture of "$endpoint/images/pets.png".

### Owners

- There should be a screen that lets you see the table of owners retrieved from GetOwners API.
- User should be able to filter the table with fuzzy searching
- Clicking on an Owner name should take you to a separate screen that lets you manage the owner with the following actions:
  1. Edit the owner, a wizard powered by UpdateOwner API
  2. Add a new pet for the owner, powered by AddPet API
- The owner management page should show a list of Pets, and be able to show you a breakdown of vet Visits (GetOwnerWithVisits API)
- You should also be able to register as a new owner, using the AddOwner API

### Veterinarians

- There should be a screen that lets you see all vets via the GetVets API