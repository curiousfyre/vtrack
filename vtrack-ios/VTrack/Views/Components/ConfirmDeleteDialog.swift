import SwiftUI

struct ConfirmDeleteDialog: ViewModifier {
    let itemName: String
    @Binding var isPresented: Bool
    let onDelete: () -> Void

    func body(content: Content) -> some View {
        content.alert("Delete \(itemName)?", isPresented: $isPresented) {
            Button("Delete", role: .destructive, action: onDelete)
            Button("Cancel", role: .cancel) {}
        } message: {
            Text("Are you sure you want to delete \(itemName)? This action cannot be undone.")
        }
    }
}

extension View {
    func confirmDelete(itemName: String, isPresented: Binding<Bool>, onDelete: @escaping () -> Void) -> some View {
        modifier(ConfirmDeleteDialog(itemName: itemName, isPresented: isPresented, onDelete: onDelete))
    }
}
