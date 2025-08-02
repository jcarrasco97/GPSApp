package com.example.gpsapp.adapter;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gpsapp.R;
import com.example.gpsapp.model.Camion;

import java.util.ArrayList;
import java.util.List;

public class CamionAdapter extends RecyclerView.Adapter<CamionAdapter.CamionViewHolder> {

    // Lista de camiones que se muestran en el RecyclerView
    private List<Camion> camiones = new ArrayList<>();

    // Listener para manejar interacciones del usuario con los elementos de la lista
    private OnCamionInteractionListener listener;

    // Interfaz para manejar acciones como editar, eliminar, mostrar geolocalización y cambiar estado activo
    public interface OnCamionInteractionListener {
        void onEditCamion(Camion camion);
        void onDeleteCamion(Camion camion);
        void onShowGeolocation(Camion camion);
        void onToggleCamionActivo(Camion camion, boolean isChecked);
    }

    // Constructor que recibe el listener para comunicar eventos a la actividad o fragmento
    public CamionAdapter(OnCamionInteractionListener listener) {
        this.listener = listener;
    }

    // Método para actualizar la lista de camiones y notificar el cambio al RecyclerView
    public void setCamiones(List<Camion> camiones) {
        this.camiones = camiones;
        notifyDataSetChanged();
    }

    // Crea nuevas vistas (invocado por el LayoutManager)
    @NonNull
    @Override
    public CamionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_camion, parent, false);
        return new CamionViewHolder(view);
    }

    // Enlaza los datos de un camión con la vista del ViewHolder correspondiente
    @Override
    public void onBindViewHolder(@NonNull CamionViewHolder holder, int position) {
        Camion camion = camiones.get(position);
        holder.textViewNombre.setText(camion.getNombre());
        holder.textViewMatricula.setText(camion.getMatricula());
        holder.switchActivo.setChecked(camion.isActivo());

        // Escucho los cambios en el switch de activo/inactivo y notifico al listener
        holder.switchActivo.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onToggleCamionActivo(camion, isChecked);
            }
        });

        // Configuro el botón de menú (tres puntos) con las opciones de editar, eliminar, y ver geolocalización
        holder.imageViewMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.imageViewMenu.getContext(), holder.imageViewMenu);
            popup.getMenuInflater().inflate(R.menu.camion_menu, popup.getMenu());

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_edit) {
                        listener.onEditCamion(camion);
                        return true;
                    } else if (itemId == R.id.action_delete) {
                        listener.onDeleteCamion(camion);
                        return true;
                    } else if (itemId == R.id.action_show_geolocation) {
                        listener.onShowGeolocation(camion);
                        return true;
                    }
                    return false;
                }
            });
            popup.show();
        });
    }

    // Devuelve el número total de elementos en la lista
    @Override
    public int getItemCount() {
        return camiones.size();
    }

    // Clase interna para el ViewHolder, contiene las vistas que se reutilizan en cada fila del RecyclerView
    static class CamionViewHolder extends RecyclerView.ViewHolder {
        TextView textViewNombre, textViewMatricula;
        Switch switchActivo;
        ImageView imageViewMenu;

        public CamionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.textViewNombreCamion);
            textViewMatricula = itemView.findViewById(R.id.textViewMatriculaCamion);
            switchActivo = itemView.findViewById(R.id.switchActivo);
            imageViewMenu = itemView.findViewById(R.id.imageViewMenu);
        }
    }
}
