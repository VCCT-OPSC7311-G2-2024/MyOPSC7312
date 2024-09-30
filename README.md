# Penny-Wise

# OPSC7312 Group 2

# Lecturer : Ambur Bruil

### Developers
- **Nabeel Van Niekerk**: ST10066882
- **Nyasha Jani**: ST10061072
- **Matthew van Niekerk**: ST10082068
- **Umar Ahmed**: ST10221775

---

## Introduction
**Penny-Wise** is a financial management application designed to help individuals track, manage, and analyze their financial transactions with ease. The app allows users to record income, expenses, and other financial activities in real-time, organizing them into categories for better insight.

With features like customizable expense categories, budgeting tools, and detailed financial reports, Penny-Wise helps users stay on top of their finances, set financial goals, and make better decisions. 

## Demonstration Video

# Download Link
https://drive.google.com/file/d/1N95LonkW_Tg9IjnOammCMxqPRS4y27Rv/view?usp=sharing

# Watch Through YouTube Link
https://youtu.be/ZaSv4A6l5G8

---

### Key Features
- **Add Expenses**: Easily input expense details, including the name and amount.
- **View Expenses**: A list view displaying all added expenses.
- **Delete Expenses**: Remove unwanted expenses.
- **User Settings**: Profile settings for updating user information and preferences.
- **Notifications**: Enable/disable notifications for profile updates.
- **Currency Conversion**: Convert amounts into different currencies, a unique feature not seen in similar applications.

---

## Technology Stack
- **Android**: Developed in Kotlin.
- **Firebase**: Used for real-time database, authentication, and cloud messaging.
- **XML**: Utilized for layout design.
- **GitHub**: Version control and collaboration platform.
- **GitHub Actions**: CI/CD setup for automated testing and deployment.

---

## Design Considerations

### User Experience (UX)
The app is designed with a focus on intuitive navigation and user-friendly interfaces. Each feature is accessible within a few clicks, ensuring that users can manage their finances effortlessly.

### Responsiveness
The app’s layout is adaptive, ensuring usability across a wide range of devices with different screen sizes.

### Security
User data is stored securely in Firebase, with authentication mechanisms in place to protect sensitive information.

---

## UI/UX and Activity Structure

### UI/UX
- Material Design components are used to provide a clean, responsive, and user-friendly interface.

### Activity Structure
- **LoginActivity**: Handles user authentication.
- **RegisterActivity**: Manages new user registration.
- **HomeActivity**: Serves as the main dashboard.
- **CurrencyConverterAPI**: Used for currency conversion.
- **Analytics**: Used to provde data on budgets and expenses of account.
- **LogExpenses**: User can create and deletes expenses for account.
- **Budget**: User is able to create many budgets for an account and delete them.

---

# Code Structure

## Architecture
The application follows the **MVVM (Model-View-ViewModel)** architecture to ensure separation of concerns, enhancing maintainability through reusable components and adherence to Kotlin best practices.

### MVVM Architecture
- **Model**: Manages data and business logic, utilizing Firebase for authentication and a custom API for currency conversion.
- **View**: Displays data and captures user interactions.
- **ViewModel**: Serves as the intermediary between the Model and the View, facilitating data flow and UI updates.

---

# Firebase Integration

The app is heavily integrated with Firebase services:

- **Firebase Realtime Database**: Stores user data such as account details.

## Firebase Setup
To set up Firebase for your project, follow these steps:

1. Add your project to Firebase via the Firebase Console.
2. Download the `google-services.json` file and place it in your app's `app/` directory.
3. Sync the project with Gradle files.

---
## GitHub Integration

We use GitHub Actions to implement a CI/CD pipeline that automates the build and test
process. This helps ensure the integrity of the codebase and enables automated testing for new
commits or pull requests.

---

## Version Control Workflow

Feature Branching: Each new feature is developed in its own branch (e.g.,
feature/authentication).

Pull Requests: Before merging into the main branch, pull requests are reviewed and tested.
CI Pipeline: GitHub Actions run automated tests on every push to ensure the build is
successful.

# How to run app

1.git clone https://github.com/username/myopsc7312.git

2.Open the project in Android Studio.

3.Add your google-services.json file in the app/ directory.
 
4.Sync the Gradle files.

5.Run the app on an Android emulator or a physical device.

---

# License
This project is licensed under the MIT License - see the LICENSE file for details.

# AI Usage

The use of AI in this project was very minimal, most usages of AI were to fix issues we had like
connecting the code to the API or Firebase.

## Conclusion
Penny-Wise provides users with a streamlined, intuitive platform to manage their financial activities, offering innovative features like spending alerts and currency conversion to keep them informed and in control of their finances.

--
